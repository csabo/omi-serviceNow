import com.hp.opr.api.ws.adapter.BulkForwardEventArgs;
import com.hp.opr.api.ws.adapter.BulkReceiveChangeArgs;
import com.hp.opr.api.ws.adapter.ForwardChangeArgs;
import com.hp.opr.api.ws.adapter.ForwardEventArgs;
import com.hp.opr.api.ws.adapter.GetExternalEventArgs;
import com.hp.opr.api.ws.adapter.InitArgs;
import com.hp.opr.api.ws.adapter.PingArgs;
import com.hp.opr.api.ws.adapter.ReceiveChangeArgs;
import com.hp.opr.api.ws.model.event.OprEvent;
import com.hp.opr.api.ws.model.event.OprEventList;
import com.hp.opr.api.ws.model.event.OprEventChange;
import com.hp.opr.api.ws.model.event.OprEventChangeList;
import com.hp.opr.api.ws.model.event.OprHistoryLineList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import groovy.json.*
import java.util.List;

class ServiceNowAdapter {
  private static final String LOG_DIR_REL = "log${File.separator}opr${File.separator}integration"
  private static final String LOGFILE_NAME = "ServiceNowAdapter.log"
  private static final JAXBContext m_jaxbContext =
  JAXBContext.newInstance(OprEventList.class, OprEventChangeList.class, OprHistoryLineList.class)

  private def m_logfile = null
  private def m_logger = null
  private InitArgs m_initArgs = null

  void init(final InitArgs args) {
    m_logger = args.logger
    m_initArgs = args

    def logfileDir = new File("${args.installDir}${File.separator}${LOG_DIR_REL}")
    if (!logfileDir.exists())
    logfileDir.mkdirs()

    m_logfile = new File(logfileDir, LOGFILE_NAME)
    if (!m_logfile.exists())
    m_logfile.createNewFile()

    m_logger.debug("Logfile Adapter initalized. INSTALL_DIR=${args.installDir}")

    def timestamp = new Date()
    def msg = """### ${timestamp.toString()}: init() called ###
    parameter connected server ID: ${m_initArgs.connectedServerId}
    parameter connected server name: ${m_initArgs.connectedServerName}
    parameter connected server display name: ${m_initArgs.connectedServerDisplayName}
    parameter node: ${m_initArgs.node}
    parameter port: ${m_initArgs.port}
    parameter ssl:  ${m_initArgs.nodeSsl}
    parameter drilldown node: ${m_initArgs.drilldownNode}
    parameter drilldown port: ${m_initArgs.drilldownPort}
    parameter drilldown ssl:  ${m_initArgs.drilldownNodeSsl}

    """
    m_logfile.append(msg)
  }

  void destroy() {
    m_logger.debug("Logfile Adapter destroy.")

    def timestamp = new Date()
    def msg = """### ${timestamp.toString()}: destroy() called ###

    """
    m_logfile.append(msg)
  }

  Boolean ping(final PingArgs args) {
    return true
  }

  Boolean forwardEvent(final ForwardEventArgs args) {
    OprEvent event = args.event
    String omiEventTitle = event.getTitle() //map to short_Description
    String omiEventDescription = event.getDescription() // Map to situation appraisal
    String omiCiHint = event.getRelatedCiHint() // map to configuration item, add to short description and description also
    def nodeHints = event.getNodeHints()
    def opServer = event.getOriginatingServer()
    String nodeName = opServer.getDnsName()
    String omiSourceCi = event.getSourceCi() // Not sure what to map this to, throw in description for now
    String omiEventId = event.getId()

    def json = JsonOutput.toJson([
      short_description: "${omiEventTitle} - ${nodeName}",
      situation_appraisal: "omi Event Desecription: ${omiEventDescription} - source CI: ${nodeName}",
      cmdb_ci: "omi CI Hint: ${nodeName}",
      u_omievent_id: "${omiEventId}",
      caller_id: "omi_integration",
      opened_by: "omi_integration",
      company: "Netsmart Technologies Inc.",
      contact_type: "System alert"
      ])
    Authenticator.setDefault (new Authenticator() {
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication ("USER", "PASS".toCharArray())
      }
      })

    def connection = new URL( "https://INSTANCE_GOES_HERE.service-now.com/api/now/table/incident" )
    .openConnection() as HttpURLConnection

    connection.setRequestProperty( 'User-Agent', 'groovy-2.4.4' )
    connection.setRequestProperty( 'Accept', 'application/json' )
    connection.setRequestProperty( 'Content-Type', 'application/json' )
    connection.setRequestMethod("POST")
    connection.doOutput = true

    def writer = new OutputStreamWriter(connection.outputStream)
    writer.write(json)
    writer.flush()
    writer.close()
    connection.connect()

    def jsonResponse = new JsonSlurper().parseText(connection.inputStream.text)
    String serviceNowSysId = jsonResponse.result.sys_id
    String externalRefId = args.setExternalRefId(serviceNowSysId)
    return true
  }

  Boolean getExternalEvent(final GetExternalEventArgs args) {
    return true
  }

  Boolean forwardChange(final ForwardChangeArgs args) {
    return true
  }

  Boolean forwardChanges(final BulkReceiveChangeArgs) {
    return true
  }

  Boolean receiveChange(final ReceiveChangeArgs args) {
    return true;
  }

  String toExternalEvent(final OprEvent event) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream()
      OutputStreamWriter osw = new OutputStreamWriter(bos, "utf-8")
      m_jaxbContext.createMarshaller().marshal(event, osw)
      return osw.toString()
      } catch (JAXBException e) {
 // TODO Auto-generated catch block
 throw new RuntimeException(e)
}
return "";
}
}
