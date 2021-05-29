/*
 *  CompilerActor.scala
 *  (Dotterweide)
 *
 *  Copyright (c) 2019 the Dotterweide authors. All rights reserved.
 *
 *  This software is published under the GNU Lesser General Public License v2.1+
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package dotterweide.metals

import java.io.{FileOutputStream, InputStream, OutputStream}
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.{util => ju}
import akka.actor.{Actor, ActorRef}
import akka.event.{Logging, LoggingAdapter}
import de.sciss.file._
import dotterweide.Span
import dotterweide.build.Version
import dotterweide.document.{LinedString, LinesHolder}
import dotterweide.editor.Aborted
import dotterweide.languages.scala.node.ThisNode
import dotterweide.metals.Implicits._
import dotterweide.node.Node
import dotterweide.node.impl.NodeImpl
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.{LanguageClient, LanguageServer}
import org.eclipse.lsp4j.{ApplyWorkspaceEditParams, ApplyWorkspaceEditResponse, ClientCapabilities, CodeActionCapabilities, CompletionCapabilities, CompletionItemCapabilities, ConfigurationParams, DefinitionCapabilities, DiagnosticSeverity, DidChangeWatchedFilesCapabilities, DocumentHighlightCapabilities, ExecuteCommandCapabilities, FormattingCapabilities, HoverCapabilities, InitializeParams, InitializeResult, InitializedParams, MessageActionItem, MessageParams, MessageType, OnTypeFormattingCapabilities, PublishDiagnosticsParams, RangeFormattingCapabilities, ReferencesCapabilities, RegistrationParams, RenameCapabilities, ShowMessageRequestParams, SignatureHelpCapabilities, SymbolCapabilities, SynchronizationCapabilities, TextDocumentClientCapabilities, UnregistrationParams, WorkspaceClientCapabilities, WorkspaceEditCapabilities, WorkspaceFolder, Range => JRange}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Promise
import scala.swing.Swing
import scala.sys.process.{Process, ProcessIO}

private object MetalsActor {
  case class Compile  (text: String)
  case class Complete (text: String, offset: Int)
  case class Type     (text: String, offset: Int)

  private case class DiaItem(range: JRange, message: String)

  private case class Diagnostics(uri: URI, list: List[DiaItem])

  private case class Compilation(fSrc: File, sender: ActorRef, text: String) {
    lazy val lines: LinesHolder = new LinedString(text)
  }
}
private class MetalsActor(scalaVersion: Version, protected val prelude: String, protected val postlude: String)
  extends Actor with LanguageClient // with ParserImpl with AdviserImpl with TypeImpl
{

  import MetalsActor._

  protected val log: LoggingAdapter = Logging(context.system, this)

  private[this] var metalsIn  : OutputStream  = _
  private[this] var metalsOut : InputStream   = _

  /*private[this] val metalsProcess: Process =*/ {
    val cmdMetals = "metals"
    val fMetals   = new File(cmdMetals)
    val pb        = Process("bash" :: cmdMetals :: Nil, cwd = Some(fMetals.getAbsoluteFile.getParentFile))
    val io        = new ProcessIO(writeIn => metalsIn = writeIn, readOut => metalsOut = readOut, _ /*readErr*/ => ())
    /*val res =*/ pb.run(io)
    assert (metalsIn != null && metalsOut != null)
    // res
  }

  private[this] val workspaceDir: File = File.createTemp(prefix = "dotterweide", directory = true)

  private[this] val fSrcRoot: File = {
    val ws = workspaceDir
    // XXX TODO figure out how to use bloop definition directly and avoid sbt
    val fProject = ws / "project"
    fProject.mkdir()
    val fProperties = fProject / "build.properties"
    writeToFile(fProperties)("sbt.version=1.2.8\n")
    val res = ws / "src" / "main" / "scala"
    res.mkdirs()
    val fBuild = ws / "build.sbt"
    writeToFile(fBuild)(
      s"""scalaVersion := "$scalaVersion"
         |""".stripMargin
    )
    res
  }

  private def writeToFile(fOut: File, encoding: String = "UTF-8")(contents: String): Unit = {
    val fos = new FileOutputStream(fOut)
    try {
      fos.write(contents.getBytes(encoding))
    } finally {
      fos.close()
    }
  }

//  // Executes `body` and returns its result to the sender.
//  // If an exception occurs, sends that exception back as `Status.Failure`
//  private def tryHandle(cmd: String)(body: => Any): Unit = {
//    val reply: Any = try {
//      log.debug(s"begin $cmd")
//      val res = body
//      log.debug(s"done $cmd")
//      res
//    } catch {
//      case e: Exception => akka.actor.Status.Failure(e)
//    }
//    sender() ! reply
//  }

  private[this] val launcher: Launcher[LanguageServer] = LSPLauncher.createClientLauncher(this, metalsOut, metalsIn)

  private[this] val server: LanguageServer = launcher.getRemoteProxy

  /*private[this] val futLauncher: Future[Void] =*/ launcher.startListening()

  connectToLanguageServer()

  def receive: Receive = {
    case Compile  (text)        => runCompile(text)
    case Diagnostics(uri, list) => runDiaResult(uri, list)
//    case Complete (text, offset)  => tryHandle("complete" )(runComplete (text, offset))
//    case Type     (text, offset)  => tryHandle("type"     )(runTypeAt   (text, offset))

    case m =>
      log.error(s"Unknown message $m")
  }

  private[this] var previousContents = ""
  private[this] var previousNode: NodeImpl = new ThisNode
  private[this] var previousSrc = Option.empty[Compilation]

  private def runDiaResult(uri: URI, list: List[DiaItem]): Unit = {
    previousSrc match {
      case Some(c) if c.fSrc.toURI == uri =>
        val n   = new NodeImpl("")
        val ln  = c.lines
        list match {
          case Nil  =>
          case more =>
            n.children = more.map { it =>
              val m = new NodeImpl("error")
              m.problem     = Some(it.message)
              val startPos  = it.range.getStart
              val endPos    = it.range.getEnd
              val startOff  = ln.startOffsetOf(startPos .getLine) + startPos.getCharacter
              val endOff    = ln.startOffsetOf(endPos   .getLine) + endPos  .getCharacter
              m.span        = Span(c.text, begin = startOff, end = endOff)
              m
            }
        }
        previousNode = n
        c.sender ! (n: Node)

      case _ =>
        println(s"Ignoring dia for $uri")
    }
  }

  private def runCompile(text0: String): Unit =
    if (previousContents == text0) {
      previousSrc match {
        case Some(c)  => previousSrc = Some(c.copy(sender = sender()))
        case None     => sender() ! previousNode
      }
    } else {
      previousSrc.foreach { c =>
//        c.fSrc.delete()
        previousSrc = None
      }
//      val fSrc    = File.createTempIn(fSrcRoot, prefix = "dotterweide", suffix = ".scala")
      val fSrc    = fSrcRoot / "dotterweide.scala"
      previousSrc = Some(Compilation(fSrc, sender(), text0))

      writeToFile(fSrc)(text0)
      previousContents = text0
    }

  private def connectToLanguageServer(): Unit = {
    // for testing, try first to use parameters like intellij-lsp

    val initParams = new InitializeParams
    initParams.setRootUri(workspaceDir.toURI.toString)

    val capCWorkspace = new WorkspaceClientCapabilities
    capCWorkspace.setApplyEdit(true)
    //      capCWorkspace.setDidChangeConfiguration(new DidChangeConfigurationCapabilities)
    capCWorkspace.setDidChangeWatchedFiles(new DidChangeWatchedFilesCapabilities)
    capCWorkspace.setExecuteCommand(new ExecuteCommandCapabilities)
    val capEWorkspace = new WorkspaceEditCapabilities
    capEWorkspace.setDocumentChanges(true)
    capCWorkspace.setWorkspaceEdit(capEWorkspace)
    capCWorkspace.setSymbol(new SymbolCapabilities)
    capCWorkspace.setWorkspaceFolders(false)
    capCWorkspace.setConfiguration(false)

    val capCTextDoc = new TextDocumentClientCapabilities
    capCTextDoc.setCodeAction(new CodeActionCapabilities)
    //      capCTextDoc.setCodeLens(new CodeLensCapabilities)
    //      capCTextDoc.setColorProvider(new ColorProviderCapabilities)
    capCTextDoc.setCompletion(new CompletionCapabilities(new CompletionItemCapabilities(false)))
    capCTextDoc.setDefinition(new DefinitionCapabilities)
    capCTextDoc.setDocumentHighlight(new DocumentHighlightCapabilities)
    //      capCTextDoc.setDocumentLink(new DocumentLinkCapabilities)
    //      capCTextDoc.setDocumentSymbol(new DocumentSymbolCapabilities)
    //      capCTextDoc.setFoldingRange(new FoldingRangeCapabilities)
    capCTextDoc.setFormatting(new FormattingCapabilities)
    capCTextDoc.setHover(new HoverCapabilities)
    //      capCTextDoc.setImplementation(new ImplementationCapabilities)
    capCTextDoc.setOnTypeFormatting(new OnTypeFormattingCapabilities)
    capCTextDoc.setRangeFormatting(new RangeFormattingCapabilities)
    capCTextDoc.setReferences(new ReferencesCapabilities)
    capCTextDoc.setRename(new RenameCapabilities)
//    capCTextDoc.setSemanticHighlightingCapabilities(new SemanticHighlightingCapabilities(false))
    capCTextDoc.setSignatureHelp(new SignatureHelpCapabilities)
    capCTextDoc.setSynchronization(new SynchronizationCapabilities(true, true, true))

    initParams.setCapabilities(new ClientCapabilities(capCWorkspace, capCTextDoc, null))
    initParams.setInitializationOptions(null)
    /*val initJFut =*/ server.initialize(initParams).thenApply((res: InitializeResult) => {
      log.debug("Done 'server.initialize'")
      server.initialized(new InitializedParams())
      res
    })
  }

  // ---- language-client interface ----

  def logMessage(message: MessageParams): Unit = {
    val lvl = message.getType match {
      case MessageType.Error    => Logging.ErrorLevel
      case MessageType.Warning  => Logging.WarningLevel
      case MessageType.Info     => Logging.InfoLevel
      case MessageType.Log      => Logging.DebugLevel
    }
    log.log(lvl, message.getMessage)
  }

  def telemetryEvent(obj: Any): Unit =
    println(s"telemetryEvent($obj)")

  def publishDiagnostics(dia: PublishDiagnosticsParams): Unit = {
    println(s"publishDiagnostics for ${dia.getUri}")
    val uri   = new URI(dia.getUri)
    val list  = dia.getDiagnostics.iterator.asScala.collect {
      // XXX TODO --- we currently collect errors only, because that's the assumption of `Node#problem`
      case d if d.getSeverity == DiagnosticSeverity.Error =>
        DiaItem(d.getRange, d.getMessage)
    } .toList
    self ! Diagnostics(uri, list) // println(s"publishDiagnostics($dia)")
  }

  def showMessage(par: MessageParams): Unit =
    println(s"showMessage($par)")

  def showMessageRequest(par: ShowMessageRequestParams): CompletableFuture[MessageActionItem] = {
    println(s"showMessageRequest($par)")
    val res = Promise[MessageActionItem]()
    Swing.onEDT {
      import scala.swing._
      val items   = par.getActions.asScala
      val entries = items.map(_.getTitle)
      val dlgTpe  = par.getType match {
        case MessageType.Error    => Dialog.Message.Error
        case MessageType.Warning  => Dialog.Message.Warning
        case MessageType.Info     => Dialog.Message.Question
        case MessageType.Log      => Dialog.Message.Plain
      }
      val dlgRes  = Dialog.showOptions(message = par.getMessage, entries = entries, messageType = dlgTpe,
        initial = 0)
      dlgRes match {
        case Dialog.Result.Closed => res.failure(Aborted())
        case other                => res.success(items(other.id))
      }
    }
    res.future
  }

  // ---- language-client overrides ----

  override def applyEdit(par: ApplyWorkspaceEditParams): CompletableFuture[ApplyWorkspaceEditResponse] = {
    println(s"applyEdit($par)")
    super.applyEdit(par)
  }

  override def registerCapability(par: RegistrationParams): CompletableFuture[Void] = {
    println(s"registerCapability($par)")
    super.registerCapability(par)
  }

  override def unregisterCapability(par: UnregistrationParams): CompletableFuture[Void] = {
    println(s"unregisterCapability($par)")
    super.unregisterCapability(par)
  }

  override def workspaceFolders(): CompletableFuture[ju.List[WorkspaceFolder]] = {
    println("workspaceFolders()")
    super.workspaceFolders()
  }

  override def configuration(par: ConfigurationParams): CompletableFuture[ju.List[AnyRef]] = {
    println(s"configuration($par)")
    super.configuration(par)
  }

//  override def semanticHighlighting(par: SemanticHighlightingParams): Unit = {
//    println(s"semanticHighlighting($par)")
//    super.semanticHighlighting(par)
//  }
}
