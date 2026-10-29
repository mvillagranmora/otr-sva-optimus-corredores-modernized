
package cl.bcs.tesoreria.webservices.recaudacion;

import java.math.BigDecimal;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the cl.bcs.tesoreria.webservices.recaudacion package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _InqSelector_QNAME = new QName("http://mfxV2_0", "InqSelector");
    private final static QName _ClientCodAdic_QNAME = new QName("http://mfxV2_0", "ClientCodAdic");
    private final static QName _ServFec_QNAME = new QName("http://mfxV2_0", "ServFec");
    private final static QName _SegRptaEncr_QNAME = new QName("http://mfxV2_0", "SegRptaEncr");
    private final static QName _SvcRecaudaTrnRq_QNAME = new QName("http://mfxV2_0", "SvcRecaudaTrnRq");
    private final static QName _ProdEmpAutzFlg_QNAME = new QName("http://mfxV2_0", "ProdEmpAutzFlg");
    private final static QName _Severidad_QNAME = new QName("http://mfxV2_0", "Severidad");
    private final static QName _ClientCtxIdVal_QNAME = new QName("http://mfxV2_0", "ClientCtxIdVal");
    private final static QName _ValFin_QNAME = new QName("http://mfxV2_0", "ValFin");
    private final static QName _MFXSvcRecaudaTrn_QNAME = new QName("http://mfxV2_0", "MFX_SvcRecaudaTrn");
    private final static QName _PagTp_QNAME = new QName("http://mfxV2_0", "PagTp");
    private final static QName _SvcRecaudaTrnRs_QNAME = new QName("http://mfxV2_0", "SvcRecaudaTrnRs");
    private final static QName _StatusCod_QNAME = new QName("http://mfxV2_0", "StatusCod");
    private final static QName _DocRef_QNAME = new QName("http://mfxV2_0", "DocRef");
    private final static QName _MsjRsHdr_QNAME = new QName("http://mfxV2_0", "MsjRsHdr");
    private final static QName _SegTokenRpta_QNAME = new QName("http://mfxV2_0", "SegTokenRpta");
    private final static QName _ClientCtxIdTp_QNAME = new QName("http://mfxV2_0", "ClientCtxIdTp");
    private final static QName _CredencialRqHdrLst_QNAME = new QName("http://mfxV2_0", "CredencialRqHdrLst");
    private final static QName _FecFin_QNAME = new QName("http://mfxV2_0", "FecFin");
    private final static QName _SvcEmpDocLst_QNAME = new QName("http://mfxV2_0", "SvcEmpDocLst");
    private final static QName _AgenciaCod_QNAME = new QName("http://mfxV2_0", "AgenciaCod");
    private final static QName _SegRol_QNAME = new QName("http://mfxV2_0", "SegRol");
    private final static QName _ExtornoFlg_QNAME = new QName("http://mfxV2_0", "ExtornoFlg");
    private final static QName _SesKey_QNAME = new QName("http://mfxV2_0", "SesKey");
    private final static QName _StatusAdic_QNAME = new QName("http://mfxV2_0", "StatusAdic");
    private final static QName _CardSec_QNAME = new QName("http://mfxV2_0", "CardSec");
    private final static QName _OpnExtornoNro_QNAME = new QName("http://mfxV2_0", "OpnExtornoNro");
    private final static QName _Status_QNAME = new QName("http://mfxV2_0", "Status");
    private final static QName _SvcEmp_QNAME = new QName("http://mfxV2_0", "SvcEmp");
    private final static QName _Login_QNAME = new QName("http://mfxV2_0", "Login");
    private final static QName _ServTrmId_QNAME = new QName("http://mfxV2_0", "ServTrmId");
    private final static QName _SvcEmpDocAdic_QNAME = new QName("http://mfxV2_0", "SvcEmpDocAdic");
    private final static QName _PagCod_QNAME = new QName("http://mfxV2_0", "PagCod");
    private final static QName _SvcSelectorLst_QNAME = new QName("http://mfxV2_0", "SvcSelectorLst");
    private final static QName _RangoFec_QNAME = new QName("http://mfxV2_0", "RangoFec");
    private final static QName _StoreForwardFlg_QNAME = new QName("http://mfxV2_0", "StoreForwardFlg");
    private final static QName _FecVct_QNAME = new QName("http://mfxV2_0", "FecVct");
    private final static QName _OpnRefAdic_QNAME = new QName("http://mfxV2_0", "OpnRefAdic");
    private final static QName _OrgCod_QNAME = new QName("http://mfxV2_0", "OrgCod");
    private final static QName _SegRqHdr_QNAME = new QName("http://mfxV2_0", "SegRqHdr");
    private final static QName _PasswordAdic_QNAME = new QName("http://mfxV2_0", "PasswordAdic");
    private final static QName _MtoTp_QNAME = new QName("http://mfxV2_0", "MtoTp");
    private final static QName _ClientTrmServ_QNAME = new QName("http://mfxV2_0", "ClientTrmServ");
    private final static QName _MedioAbono_QNAME = new QName("http://mfxV2_0", "MedioAbono");
    private final static QName _MonCod_QNAME = new QName("http://mfxV2_0", "MonCod");
    private final static QName _TrnNivelAutz_QNAME = new QName("http://mfxV2_0", "TrnNivelAutz");
    private final static QName _ClientTrmApp_QNAME = new QName("http://mfxV2_0", "ClientTrmApp");
    private final static QName _ReenvioFlg_QNAME = new QName("http://mfxV2_0", "ReenvioFlg");
    private final static QName _DocTp_QNAME = new QName("http://mfxV2_0", "DocTp");
    private final static QName _SvcEmpDoc_QNAME = new QName("http://mfxV2_0", "SvcEmpDoc");
    private final static QName _MtoRangoValMax_QNAME = new QName("http://mfxV2_0", "MtoRangoValMax");
    private final static QName _SegTokenKey_QNAME = new QName("http://mfxV2_0", "SegTokenKey");
    private final static QName _PasswordVal_QNAME = new QName("http://mfxV2_0", "PasswordVal");
    private final static QName _OpnNro_QNAME = new QName("http://mfxV2_0", "OpnNro");
    private final static QName _RegRqCtrl_QNAME = new QName("http://mfxV2_0", "RegRqCtrl");
    private final static QName _ClientProxy_QNAME = new QName("http://mfxV2_0", "ClientProxy");
    private final static QName _ClientCod_QNAME = new QName("http://mfxV2_0", "ClientCod");
    private final static QName _SegTokenLogin_QNAME = new QName("http://mfxV2_0", "SegTokenLogin");
    private final static QName _CardInqRef_QNAME = new QName("http://mfxV2_0", "CardInqRef");
    private final static QName _MtoDoc_QNAME = new QName("http://mfxV2_0", "MtoDoc");
    private final static QName _TrnExtorno_QNAME = new QName("http://mfxV2_0", "TrnExtorno");
    private final static QName _ClientFec_QNAME = new QName("http://mfxV2_0", "ClientFec");
    private final static QName _ValIni_QNAME = new QName("http://mfxV2_0", "ValIni");
    private final static QName _SvcSelector_QNAME = new QName("http://mfxV2_0", "SvcSelector");
    private final static QName _EncrTp_QNAME = new QName("http://mfxV2_0", "EncrTp");
    private final static QName _SPCod_QNAME = new QName("http://mfxV2_0", "SPCod");
    private final static QName _SegRsHdr_QNAME = new QName("http://mfxV2_0", "SegRsHdr");
    private final static QName _CtdMax_QNAME = new QName("http://mfxV2_0", "CtdMax");
    private final static QName _SvcRecaudaInqRef_QNAME = new QName("http://mfxV2_0", "SvcRecaudaInqRef");
    private final static QName _SvcRecaudaInqReg_QNAME = new QName("http://mfxV2_0", "SvcRecaudaInqReg");
    private final static QName _MtoMon_QNAME = new QName("http://mfxV2_0", "MtoMon");
    private final static QName _Ver_QNAME = new QName("http://mfxV2_0", "Ver");
    private final static QName _MtoVal_QNAME = new QName("http://mfxV2_0", "MtoVal");
    private final static QName _SegTokenUsr_QNAME = new QName("http://mfxV2_0", "SegTokenUsr");
    private final static QName _TpCursor_QNAME = new QName("http://mfxV2_0", "TpCursor");
    private final static QName _RangoVal_QNAME = new QName("http://mfxV2_0", "RangoVal");
    private final static QName _Mto_QNAME = new QName("http://mfxV2_0", "Mto");
    private final static QName _TrnSelector_QNAME = new QName("http://mfxV2_0", "TrnSelector");
    private final static QName _CredencialRsHdr_QNAME = new QName("http://mfxV2_0", "CredencialRsHdr");
    private final static QName _ClientTrmId_QNAME = new QName("http://mfxV2_0", "ClientTrmId");
    private final static QName _Cursor_QNAME = new QName("http://mfxV2_0", "Cursor");
    private final static QName _SvcEmpLst_QNAME = new QName("http://mfxV2_0", "SvcEmpLst");
    private final static QName _SvcId_QNAME = new QName("http://mfxV2_0", "SvcId");
    private final static QName _ClsEstProdCod_QNAME = new QName("http://mfxV2_0", "ClsEstProdCod");
    private final static QName _MtoRangoValMin_QNAME = new QName("http://mfxV2_0", "MtoRangoValMin");
    private final static QName _OpnNroAdic_QNAME = new QName("http://mfxV2_0", "OpnNroAdic");
    private final static QName _ChqNro_QNAME = new QName("http://mfxV2_0", "ChqNro");
    private final static QName _PasswordEncr_QNAME = new QName("http://mfxV2_0", "PasswordEncr");
    private final static QName _DocId_QNAME = new QName("http://mfxV2_0", "DocId");
    private final static QName _DocNro_QNAME = new QName("http://mfxV2_0", "DocNro");
    private final static QName _MedioAbonoCod_QNAME = new QName("http://mfxV2_0", "MedioAbonoCod");
    private final static QName _PagId_QNAME = new QName("http://mfxV2_0", "PagId");
    private final static QName _GenSesTokenFlg_QNAME = new QName("http://mfxV2_0", "GenSesTokenFlg");
    private final static QName _ServStatus_QNAME = new QName("http://mfxV2_0", "ServStatus");
    private final static QName _StatusDes_QNAME = new QName("http://mfxV2_0", "StatusDes");
    private final static QName _OpnRefAdicLst_QNAME = new QName("http://mfxV2_0", "OpnRefAdicLst");
    private final static QName _NroSec_QNAME = new QName("http://mfxV2_0", "NroSec");
    private final static QName _SegRpta_QNAME = new QName("http://mfxV2_0", "SegRpta");
    private final static QName _FecIni_QNAME = new QName("http://mfxV2_0", "FecIni");
    private final static QName _SucursalCod_QNAME = new QName("http://mfxV2_0", "SucursalCod");
    private final static QName _MtoDocLst_QNAME = new QName("http://mfxV2_0", "MtoDocLst");
    private final static QName _Password_QNAME = new QName("http://mfxV2_0", "Password");
    private final static QName _ServStatusCod_QNAME = new QName("http://mfxV2_0", "ServStatusCod");
    private final static QName _SvcEmpDocAdicLst_QNAME = new QName("http://mfxV2_0", "SvcEmpDocAdicLst");
    private final static QName _FecEmision_QNAME = new QName("http://mfxV2_0", "FecEmision");
    private final static QName _NomCompleto_QNAME = new QName("http://mfxV2_0", "NomCompleto");
    private final static QName _ClientApp_QNAME = new QName("http://mfxV2_0", "ClientApp");
    private final static QName _SegSecreto_QNAME = new QName("http://mfxV2_0", "SegSecreto");
    private final static QName _SvcRecaudaTrnRef_QNAME = new QName("http://mfxV2_0", "SvcRecaudaTrnRef");
    private final static QName _SvcRecaudaTrnReg_QNAME = new QName("http://mfxV2_0", "SvcRecaudaTrnReg");
    private final static QName _RqUID_QNAME = new QName("http://mfxV2_0", "RqUID");
    private final static QName _CtxRqHdr_QNAME = new QName("http://mfxV2_0", "CtxRqHdr");
    private final static QName _CredencialRqHdr_QNAME = new QName("http://mfxV2_0", "CredencialRqHdr");
    private final static QName _SegTokenCard_QNAME = new QName("http://mfxV2_0", "SegTokenCard");
    private final static QName _MsjRqHdr_QNAME = new QName("http://mfxV2_0", "MsjRqHdr");
    private final static QName _Des_QNAME = new QName("http://mfxV2_0", "Des");
    private final static QName _ClientCtxId_QNAME = new QName("http://mfxV2_0", "ClientCtxId");
    private final static QName _Ctd_QNAME = new QName("http://mfxV2_0", "Ctd");
    private final static QName _ProdNro_QNAME = new QName("http://mfxV2_0", "ProdNro");
    private final static QName _RangoMto_QNAME = new QName("http://mfxV2_0", "RangoMto");
    private final static QName _TrnTpLst_QNAME = new QName("http://mfxV2_0", "TrnTpLst");
    private final static QName _ExtornoTp_QNAME = new QName("http://mfxV2_0", "ExtornoTp");
    private final static QName _ProdEspAutzFlg_QNAME = new QName("http://mfxV2_0", "ProdEspAutzFlg");
    private final static QName _TrnTp_QNAME = new QName("http://mfxV2_0", "TrnTp");
    private final static QName _StatusAdicLst_QNAME = new QName("http://mfxV2_0", "StatusAdicLst");
    private final static QName _LngPgn_QNAME = new QName("http://mfxV2_0", "LngPgn");
    private final static QName _ClientCtxIdLst_QNAME = new QName("http://mfxV2_0", "ClientCtxIdLst");
    private final static QName _OpnRef_QNAME = new QName("http://mfxV2_0", "OpnRef");
    private final static QName _CtxRsHdr_QNAME = new QName("http://mfxV2_0", "CtxRsHdr");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: cl.bcs.tesoreria.webservices.recaudacion
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MFXSvcRecaudaTrnType }
     * 
     */
    public MFXSvcRecaudaTrnType createMFXSvcRecaudaTrnType() {
        return new MFXSvcRecaudaTrnType();
    }

    /**
     * Create an instance of {@link SvcRecaudaTrnRsType }
     * 
     */
    public SvcRecaudaTrnRsType createSvcRecaudaTrnRsType() {
        return new SvcRecaudaTrnRsType();
    }

    /**
     * Create an instance of {@link InqSelectorType }
     * 
     */
    public InqSelectorType createInqSelectorType() {
        return new InqSelectorType();
    }

    /**
     * Create an instance of {@link SvcRecaudaTrnRqType }
     * 
     */
    public SvcRecaudaTrnRqType createSvcRecaudaTrnRqType() {
        return new SvcRecaudaTrnRqType();
    }

    /**
     * Create an instance of {@link StatusType }
     * 
     */
    public StatusType createStatusType() {
        return new StatusType();
    }

    /**
     * Create an instance of {@link SvcEmpType }
     * 
     */
    public SvcEmpType createSvcEmpType() {
        return new SvcEmpType();
    }

    /**
     * Create an instance of {@link SvcEmpDocAdicType }
     * 
     */
    public SvcEmpDocAdicType createSvcEmpDocAdicType() {
        return new SvcEmpDocAdicType();
    }

    /**
     * Create an instance of {@link RangoFecType }
     * 
     */
    public RangoFecType createRangoFecType() {
        return new RangoFecType();
    }

    /**
     * Create an instance of {@link SvcSelectorLstType }
     * 
     */
    public SvcSelectorLstType createSvcSelectorLstType() {
        return new SvcSelectorLstType();
    }

    /**
     * Create an instance of {@link MsjRsHdrType }
     * 
     */
    public MsjRsHdrType createMsjRsHdrType() {
        return new MsjRsHdrType();
    }

    /**
     * Create an instance of {@link SegTokenRptaType }
     * 
     */
    public SegTokenRptaType createSegTokenRptaType() {
        return new SegTokenRptaType();
    }

    /**
     * Create an instance of {@link SvcEmpDocLstType }
     * 
     */
    public SvcEmpDocLstType createSvcEmpDocLstType() {
        return new SvcEmpDocLstType();
    }

    /**
     * Create an instance of {@link CredencialRqHdrLstType }
     * 
     */
    public CredencialRqHdrLstType createCredencialRqHdrLstType() {
        return new CredencialRqHdrLstType();
    }

    /**
     * Create an instance of {@link StatusAdicType }
     * 
     */
    public StatusAdicType createStatusAdicType() {
        return new StatusAdicType();
    }

    /**
     * Create an instance of {@link SvcEmpDocType }
     * 
     */
    public SvcEmpDocType createSvcEmpDocType() {
        return new SvcEmpDocType();
    }

    /**
     * Create an instance of {@link SegTokenKeyType }
     * 
     */
    public SegTokenKeyType createSegTokenKeyType() {
        return new SegTokenKeyType();
    }

    /**
     * Create an instance of {@link SegRqHdrType }
     * 
     */
    public SegRqHdrType createSegRqHdrType() {
        return new SegRqHdrType();
    }

    /**
     * Create an instance of {@link MedioAbonoType }
     * 
     */
    public MedioAbonoType createMedioAbonoType() {
        return new MedioAbonoType();
    }

    /**
     * Create an instance of {@link SegTokenLoginType }
     * 
     */
    public SegTokenLoginType createSegTokenLoginType() {
        return new SegTokenLoginType();
    }

    /**
     * Create an instance of {@link CardInqRefType }
     * 
     */
    public CardInqRefType createCardInqRefType() {
        return new CardInqRefType();
    }

    /**
     * Create an instance of {@link MtoDocType }
     * 
     */
    public MtoDocType createMtoDocType() {
        return new MtoDocType();
    }

    /**
     * Create an instance of {@link TrnExtornoType }
     * 
     */
    public TrnExtornoType createTrnExtornoType() {
        return new TrnExtornoType();
    }

    /**
     * Create an instance of {@link RegRqCtrlType }
     * 
     */
    public RegRqCtrlType createRegRqCtrlType() {
        return new RegRqCtrlType();
    }

    /**
     * Create an instance of {@link ClientProxyType }
     * 
     */
    public ClientProxyType createClientProxyType() {
        return new ClientProxyType();
    }

    /**
     * Create an instance of {@link MtoMonType }
     * 
     */
    public MtoMonType createMtoMonType() {
        return new MtoMonType();
    }

    /**
     * Create an instance of {@link SegTokenUsrType }
     * 
     */
    public SegTokenUsrType createSegTokenUsrType() {
        return new SegTokenUsrType();
    }

    /**
     * Create an instance of {@link TrnSelectorType }
     * 
     */
    public TrnSelectorType createTrnSelectorType() {
        return new TrnSelectorType();
    }

    /**
     * Create an instance of {@link RangoValType }
     * 
     */
    public RangoValType createRangoValType() {
        return new RangoValType();
    }

    /**
     * Create an instance of {@link ClientTrmIdType }
     * 
     */
    public ClientTrmIdType createClientTrmIdType() {
        return new ClientTrmIdType();
    }

    /**
     * Create an instance of {@link CredencialRsHdrType }
     * 
     */
    public CredencialRsHdrType createCredencialRsHdrType() {
        return new CredencialRsHdrType();
    }

    /**
     * Create an instance of {@link SegRsHdrType }
     * 
     */
    public SegRsHdrType createSegRsHdrType() {
        return new SegRsHdrType();
    }

    /**
     * Create an instance of {@link SvcRecaudaInqRegType }
     * 
     */
    public SvcRecaudaInqRegType createSvcRecaudaInqRegType() {
        return new SvcRecaudaInqRegType();
    }

    /**
     * Create an instance of {@link SvcRecaudaInqRefType }
     * 
     */
    public SvcRecaudaInqRefType createSvcRecaudaInqRefType() {
        return new SvcRecaudaInqRefType();
    }

    /**
     * Create an instance of {@link ServStatusType }
     * 
     */
    public ServStatusType createServStatusType() {
        return new ServStatusType();
    }

    /**
     * Create an instance of {@link SvcEmpLstType }
     * 
     */
    public SvcEmpLstType createSvcEmpLstType() {
        return new SvcEmpLstType();
    }

    /**
     * Create an instance of {@link ClientAppType }
     * 
     */
    public ClientAppType createClientAppType() {
        return new ClientAppType();
    }

    /**
     * Create an instance of {@link SvcEmpDocAdicLstType }
     * 
     */
    public SvcEmpDocAdicLstType createSvcEmpDocAdicLstType() {
        return new SvcEmpDocAdicLstType();
    }

    /**
     * Create an instance of {@link SvcRecaudaTrnRegType }
     * 
     */
    public SvcRecaudaTrnRegType createSvcRecaudaTrnRegType() {
        return new SvcRecaudaTrnRegType();
    }

    /**
     * Create an instance of {@link SvcRecaudaTrnRefType }
     * 
     */
    public SvcRecaudaTrnRefType createSvcRecaudaTrnRefType() {
        return new SvcRecaudaTrnRefType();
    }

    /**
     * Create an instance of {@link SegTokenCardType }
     * 
     */
    public SegTokenCardType createSegTokenCardType() {
        return new SegTokenCardType();
    }

    /**
     * Create an instance of {@link CredencialRqHdrType }
     * 
     */
    public CredencialRqHdrType createCredencialRqHdrType() {
        return new CredencialRqHdrType();
    }

    /**
     * Create an instance of {@link CtxRqHdrType }
     * 
     */
    public CtxRqHdrType createCtxRqHdrType() {
        return new CtxRqHdrType();
    }

    /**
     * Create an instance of {@link OpnRefAdicLstType }
     * 
     */
    public OpnRefAdicLstType createOpnRefAdicLstType() {
        return new OpnRefAdicLstType();
    }

    /**
     * Create an instance of {@link MtoDocLstType }
     * 
     */
    public MtoDocLstType createMtoDocLstType() {
        return new MtoDocLstType();
    }

    /**
     * Create an instance of {@link PasswordType }
     * 
     */
    public PasswordType createPasswordType() {
        return new PasswordType();
    }

    /**
     * Create an instance of {@link ClientCtxIdLstType }
     * 
     */
    public ClientCtxIdLstType createClientCtxIdLstType() {
        return new ClientCtxIdLstType();
    }

    /**
     * Create an instance of {@link CtxRsHdrType }
     * 
     */
    public CtxRsHdrType createCtxRsHdrType() {
        return new CtxRsHdrType();
    }

    /**
     * Create an instance of {@link RangoMtoType }
     * 
     */
    public RangoMtoType createRangoMtoType() {
        return new RangoMtoType();
    }

    /**
     * Create an instance of {@link TrnTpLstType }
     * 
     */
    public TrnTpLstType createTrnTpLstType() {
        return new TrnTpLstType();
    }

    /**
     * Create an instance of {@link ClientCtxIdType }
     * 
     */
    public ClientCtxIdType createClientCtxIdType() {
        return new ClientCtxIdType();
    }

    /**
     * Create an instance of {@link MsjRqHdrType }
     * 
     */
    public MsjRqHdrType createMsjRqHdrType() {
        return new MsjRqHdrType();
    }

    /**
     * Create an instance of {@link StatusAdicLstType }
     * 
     */
    public StatusAdicLstType createStatusAdicLstType() {
        return new StatusAdicLstType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InqSelectorType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "InqSelector")
    public JAXBElement<InqSelectorType> createInqSelector(InqSelectorType value) {
        return new JAXBElement<InqSelectorType>(_InqSelector_QNAME, InqSelectorType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ClientCodAdic")
    public JAXBElement<String> createClientCodAdic(String value) {
        return new JAXBElement<String>(_ClientCodAdic_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ServFec")
    public JAXBElement<XMLGregorianCalendar> createServFec(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_ServFec_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SegRptaEncr")
    public JAXBElement<String> createSegRptaEncr(String value) {
        return new JAXBElement<String>(_SegRptaEncr_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SvcRecaudaTrnRqType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcRecaudaTrnRq")
    public JAXBElement<SvcRecaudaTrnRqType> createSvcRecaudaTrnRq(SvcRecaudaTrnRqType value) {
        return new JAXBElement<SvcRecaudaTrnRqType>(_SvcRecaudaTrnRq_QNAME, SvcRecaudaTrnRqType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ProdEmpAutzFlg")
    public JAXBElement<String> createProdEmpAutzFlg(String value) {
        return new JAXBElement<String>(_ProdEmpAutzFlg_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SeveridadType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "Severidad")
    public JAXBElement<SeveridadType> createSeveridad(SeveridadType value) {
        return new JAXBElement<SeveridadType>(_Severidad_QNAME, SeveridadType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ClientCtxIdVal")
    public JAXBElement<String> createClientCtxIdVal(String value) {
        return new JAXBElement<String>(_ClientCtxIdVal_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ValFin")
    public JAXBElement<String> createValFin(String value) {
        return new JAXBElement<String>(_ValFin_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MFXSvcRecaudaTrnType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "MFX_SvcRecaudaTrn")
    public JAXBElement<MFXSvcRecaudaTrnType> createMFXSvcRecaudaTrn(MFXSvcRecaudaTrnType value) {
        return new JAXBElement<MFXSvcRecaudaTrnType>(_MFXSvcRecaudaTrn_QNAME, MFXSvcRecaudaTrnType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "PagTp")
    public JAXBElement<String> createPagTp(String value) {
        return new JAXBElement<String>(_PagTp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SvcRecaudaTrnRsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcRecaudaTrnRs")
    public JAXBElement<SvcRecaudaTrnRsType> createSvcRecaudaTrnRs(SvcRecaudaTrnRsType value) {
        return new JAXBElement<SvcRecaudaTrnRsType>(_SvcRecaudaTrnRs_QNAME, SvcRecaudaTrnRsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "StatusCod")
    public JAXBElement<String> createStatusCod(String value) {
        return new JAXBElement<String>(_StatusCod_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "DocRef")
    public JAXBElement<String> createDocRef(String value) {
        return new JAXBElement<String>(_DocRef_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MsjRsHdrType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "MsjRsHdr")
    public JAXBElement<MsjRsHdrType> createMsjRsHdr(MsjRsHdrType value) {
        return new JAXBElement<MsjRsHdrType>(_MsjRsHdr_QNAME, MsjRsHdrType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SegTokenRptaType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SegTokenRpta")
    public JAXBElement<SegTokenRptaType> createSegTokenRpta(SegTokenRptaType value) {
        return new JAXBElement<SegTokenRptaType>(_SegTokenRpta_QNAME, SegTokenRptaType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ClientCtxIdTp")
    public JAXBElement<String> createClientCtxIdTp(String value) {
        return new JAXBElement<String>(_ClientCtxIdTp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CredencialRqHdrLstType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "CredencialRqHdrLst")
    public JAXBElement<CredencialRqHdrLstType> createCredencialRqHdrLst(CredencialRqHdrLstType value) {
        return new JAXBElement<CredencialRqHdrLstType>(_CredencialRqHdrLst_QNAME, CredencialRqHdrLstType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "FecFin")
    public JAXBElement<String> createFecFin(String value) {
        return new JAXBElement<String>(_FecFin_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SvcEmpDocLstType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcEmpDocLst")
    public JAXBElement<SvcEmpDocLstType> createSvcEmpDocLst(SvcEmpDocLstType value) {
        return new JAXBElement<SvcEmpDocLstType>(_SvcEmpDocLst_QNAME, SvcEmpDocLstType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "AgenciaCod")
    public JAXBElement<String> createAgenciaCod(String value) {
        return new JAXBElement<String>(_AgenciaCod_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SegRolType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SegRol")
    public JAXBElement<SegRolType> createSegRol(SegRolType value) {
        return new JAXBElement<SegRolType>(_SegRol_QNAME, SegRolType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ExtornoFlg")
    public JAXBElement<String> createExtornoFlg(String value) {
        return new JAXBElement<String>(_ExtornoFlg_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SesKey")
    public JAXBElement<String> createSesKey(String value) {
        return new JAXBElement<String>(_SesKey_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StatusAdicType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "StatusAdic")
    public JAXBElement<StatusAdicType> createStatusAdic(StatusAdicType value) {
        return new JAXBElement<StatusAdicType>(_StatusAdic_QNAME, StatusAdicType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "CardSec")
    public JAXBElement<String> createCardSec(String value) {
        return new JAXBElement<String>(_CardSec_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "OpnExtornoNro")
    public JAXBElement<String> createOpnExtornoNro(String value) {
        return new JAXBElement<String>(_OpnExtornoNro_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StatusType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "Status")
    public JAXBElement<StatusType> createStatus(StatusType value) {
        return new JAXBElement<StatusType>(_Status_QNAME, StatusType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SvcEmpType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcEmp")
    public JAXBElement<SvcEmpType> createSvcEmp(SvcEmpType value) {
        return new JAXBElement<SvcEmpType>(_SvcEmp_QNAME, SvcEmpType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "Login")
    public JAXBElement<String> createLogin(String value) {
        return new JAXBElement<String>(_Login_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ServTrmId")
    public JAXBElement<String> createServTrmId(String value) {
        return new JAXBElement<String>(_ServTrmId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SvcEmpDocAdicType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcEmpDocAdic")
    public JAXBElement<SvcEmpDocAdicType> createSvcEmpDocAdic(SvcEmpDocAdicType value) {
        return new JAXBElement<SvcEmpDocAdicType>(_SvcEmpDocAdic_QNAME, SvcEmpDocAdicType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "PagCod")
    public JAXBElement<String> createPagCod(String value) {
        return new JAXBElement<String>(_PagCod_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SvcSelectorLstType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcSelectorLst")
    public JAXBElement<SvcSelectorLstType> createSvcSelectorLst(SvcSelectorLstType value) {
        return new JAXBElement<SvcSelectorLstType>(_SvcSelectorLst_QNAME, SvcSelectorLstType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RangoFecType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "RangoFec")
    public JAXBElement<RangoFecType> createRangoFec(RangoFecType value) {
        return new JAXBElement<RangoFecType>(_RangoFec_QNAME, RangoFecType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "StoreForwardFlg")
    public JAXBElement<String> createStoreForwardFlg(String value) {
        return new JAXBElement<String>(_StoreForwardFlg_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "FecVct")
    public JAXBElement<String> createFecVct(String value) {
        return new JAXBElement<String>(_FecVct_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "OpnRefAdic")
    public JAXBElement<String> createOpnRefAdic(String value) {
        return new JAXBElement<String>(_OpnRefAdic_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "OrgCod")
    public JAXBElement<String> createOrgCod(String value) {
        return new JAXBElement<String>(_OrgCod_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SegRqHdrType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SegRqHdr")
    public JAXBElement<SegRqHdrType> createSegRqHdr(SegRqHdrType value) {
        return new JAXBElement<SegRqHdrType>(_SegRqHdr_QNAME, SegRqHdrType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "PasswordAdic")
    public JAXBElement<String> createPasswordAdic(String value) {
        return new JAXBElement<String>(_PasswordAdic_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MtoTpType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "MtoTp")
    public JAXBElement<MtoTpType> createMtoTp(MtoTpType value) {
        return new JAXBElement<MtoTpType>(_MtoTp_QNAME, MtoTpType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ClientTrmServ")
    public JAXBElement<String> createClientTrmServ(String value) {
        return new JAXBElement<String>(_ClientTrmServ_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MedioAbonoType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "MedioAbono")
    public JAXBElement<MedioAbonoType> createMedioAbono(MedioAbonoType value) {
        return new JAXBElement<MedioAbonoType>(_MedioAbono_QNAME, MedioAbonoType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "MonCod")
    public JAXBElement<String> createMonCod(String value) {
        return new JAXBElement<String>(_MonCod_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TrnNivelAutzType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "TrnNivelAutz")
    public JAXBElement<TrnNivelAutzType> createTrnNivelAutz(TrnNivelAutzType value) {
        return new JAXBElement<TrnNivelAutzType>(_TrnNivelAutz_QNAME, TrnNivelAutzType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ClientTrmApp")
    public JAXBElement<String> createClientTrmApp(String value) {
        return new JAXBElement<String>(_ClientTrmApp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ReenvioFlg")
    public JAXBElement<String> createReenvioFlg(String value) {
        return new JAXBElement<String>(_ReenvioFlg_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "DocTp")
    public JAXBElement<String> createDocTp(String value) {
        return new JAXBElement<String>(_DocTp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SvcEmpDocType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcEmpDoc")
    public JAXBElement<SvcEmpDocType> createSvcEmpDoc(SvcEmpDocType value) {
        return new JAXBElement<SvcEmpDocType>(_SvcEmpDoc_QNAME, SvcEmpDocType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "MtoRangoValMax")
    public JAXBElement<BigDecimal> createMtoRangoValMax(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_MtoRangoValMax_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SegTokenKeyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SegTokenKey")
    public JAXBElement<SegTokenKeyType> createSegTokenKey(SegTokenKeyType value) {
        return new JAXBElement<SegTokenKeyType>(_SegTokenKey_QNAME, SegTokenKeyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "PasswordVal")
    public JAXBElement<String> createPasswordVal(String value) {
        return new JAXBElement<String>(_PasswordVal_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "OpnNro")
    public JAXBElement<String> createOpnNro(String value) {
        return new JAXBElement<String>(_OpnNro_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegRqCtrlType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "RegRqCtrl")
    public JAXBElement<RegRqCtrlType> createRegRqCtrl(RegRqCtrlType value) {
        return new JAXBElement<RegRqCtrlType>(_RegRqCtrl_QNAME, RegRqCtrlType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClientProxyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ClientProxy")
    public JAXBElement<ClientProxyType> createClientProxy(ClientProxyType value) {
        return new JAXBElement<ClientProxyType>(_ClientProxy_QNAME, ClientProxyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ClientCod")
    public JAXBElement<String> createClientCod(String value) {
        return new JAXBElement<String>(_ClientCod_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SegTokenLoginType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SegTokenLogin")
    public JAXBElement<SegTokenLoginType> createSegTokenLogin(SegTokenLoginType value) {
        return new JAXBElement<SegTokenLoginType>(_SegTokenLogin_QNAME, SegTokenLoginType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CardInqRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "CardInqRef")
    public JAXBElement<CardInqRefType> createCardInqRef(CardInqRefType value) {
        return new JAXBElement<CardInqRefType>(_CardInqRef_QNAME, CardInqRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MtoDocType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "MtoDoc")
    public JAXBElement<MtoDocType> createMtoDoc(MtoDocType value) {
        return new JAXBElement<MtoDocType>(_MtoDoc_QNAME, MtoDocType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TrnExtornoType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "TrnExtorno")
    public JAXBElement<TrnExtornoType> createTrnExtorno(TrnExtornoType value) {
        return new JAXBElement<TrnExtornoType>(_TrnExtorno_QNAME, TrnExtornoType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ClientFec")
    public JAXBElement<XMLGregorianCalendar> createClientFec(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_ClientFec_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ValIni")
    public JAXBElement<String> createValIni(String value) {
        return new JAXBElement<String>(_ValIni_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcSelector")
    public JAXBElement<String> createSvcSelector(String value) {
        return new JAXBElement<String>(_SvcSelector_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "EncrTp")
    public JAXBElement<String> createEncrTp(String value) {
        return new JAXBElement<String>(_EncrTp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SPCod")
    public JAXBElement<String> createSPCod(String value) {
        return new JAXBElement<String>(_SPCod_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SegRsHdrType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SegRsHdr")
    public JAXBElement<SegRsHdrType> createSegRsHdr(SegRsHdrType value) {
        return new JAXBElement<SegRsHdrType>(_SegRsHdr_QNAME, SegRsHdrType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "CtdMax")
    public JAXBElement<Long> createCtdMax(Long value) {
        return new JAXBElement<Long>(_CtdMax_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SvcRecaudaInqRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcRecaudaInqRef")
    public JAXBElement<SvcRecaudaInqRefType> createSvcRecaudaInqRef(SvcRecaudaInqRefType value) {
        return new JAXBElement<SvcRecaudaInqRefType>(_SvcRecaudaInqRef_QNAME, SvcRecaudaInqRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SvcRecaudaInqRegType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcRecaudaInqReg")
    public JAXBElement<SvcRecaudaInqRegType> createSvcRecaudaInqReg(SvcRecaudaInqRegType value) {
        return new JAXBElement<SvcRecaudaInqRegType>(_SvcRecaudaInqReg_QNAME, SvcRecaudaInqRegType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MtoMonType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "MtoMon")
    public JAXBElement<MtoMonType> createMtoMon(MtoMonType value) {
        return new JAXBElement<MtoMonType>(_MtoMon_QNAME, MtoMonType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "Ver")
    public JAXBElement<String> createVer(String value) {
        return new JAXBElement<String>(_Ver_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MtoMonType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "MtoVal")
    public JAXBElement<MtoMonType> createMtoVal(MtoMonType value) {
        return new JAXBElement<MtoMonType>(_MtoVal_QNAME, MtoMonType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SegTokenUsrType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SegTokenUsr")
    public JAXBElement<SegTokenUsrType> createSegTokenUsr(SegTokenUsrType value) {
        return new JAXBElement<SegTokenUsrType>(_SegTokenUsr_QNAME, SegTokenUsrType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "TpCursor")
    public JAXBElement<String> createTpCursor(String value) {
        return new JAXBElement<String>(_TpCursor_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RangoValType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "RangoVal")
    public JAXBElement<RangoValType> createRangoVal(RangoValType value) {
        return new JAXBElement<RangoValType>(_RangoVal_QNAME, RangoValType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "Mto")
    public JAXBElement<BigDecimal> createMto(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_Mto_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TrnSelectorType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "TrnSelector")
    public JAXBElement<TrnSelectorType> createTrnSelector(TrnSelectorType value) {
        return new JAXBElement<TrnSelectorType>(_TrnSelector_QNAME, TrnSelectorType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CredencialRsHdrType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "CredencialRsHdr")
    public JAXBElement<CredencialRsHdrType> createCredencialRsHdr(CredencialRsHdrType value) {
        return new JAXBElement<CredencialRsHdrType>(_CredencialRsHdr_QNAME, CredencialRsHdrType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClientTrmIdType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ClientTrmId")
    public JAXBElement<ClientTrmIdType> createClientTrmId(ClientTrmIdType value) {
        return new JAXBElement<ClientTrmIdType>(_ClientTrmId_QNAME, ClientTrmIdType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "Cursor")
    public JAXBElement<String> createCursor(String value) {
        return new JAXBElement<String>(_Cursor_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SvcEmpLstType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcEmpLst")
    public JAXBElement<SvcEmpLstType> createSvcEmpLst(SvcEmpLstType value) {
        return new JAXBElement<SvcEmpLstType>(_SvcEmpLst_QNAME, SvcEmpLstType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcId")
    public JAXBElement<String> createSvcId(String value) {
        return new JAXBElement<String>(_SvcId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ClsEstProdCod")
    public JAXBElement<String> createClsEstProdCod(String value) {
        return new JAXBElement<String>(_ClsEstProdCod_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "MtoRangoValMin")
    public JAXBElement<BigDecimal> createMtoRangoValMin(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_MtoRangoValMin_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "OpnNroAdic")
    public JAXBElement<String> createOpnNroAdic(String value) {
        return new JAXBElement<String>(_OpnNroAdic_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ChqNro")
    public JAXBElement<String> createChqNro(String value) {
        return new JAXBElement<String>(_ChqNro_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "PasswordEncr")
    public JAXBElement<String> createPasswordEncr(String value) {
        return new JAXBElement<String>(_PasswordEncr_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "DocId")
    public JAXBElement<String> createDocId(String value) {
        return new JAXBElement<String>(_DocId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "DocNro")
    public JAXBElement<String> createDocNro(String value) {
        return new JAXBElement<String>(_DocNro_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "MedioAbonoCod")
    public JAXBElement<String> createMedioAbonoCod(String value) {
        return new JAXBElement<String>(_MedioAbonoCod_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "PagId")
    public JAXBElement<String> createPagId(String value) {
        return new JAXBElement<String>(_PagId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "GenSesTokenFlg")
    public JAXBElement<String> createGenSesTokenFlg(String value) {
        return new JAXBElement<String>(_GenSesTokenFlg_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServStatusType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ServStatus")
    public JAXBElement<ServStatusType> createServStatus(ServStatusType value) {
        return new JAXBElement<ServStatusType>(_ServStatus_QNAME, ServStatusType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "StatusDes")
    public JAXBElement<String> createStatusDes(String value) {
        return new JAXBElement<String>(_StatusDes_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OpnRefAdicLstType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "OpnRefAdicLst")
    public JAXBElement<OpnRefAdicLstType> createOpnRefAdicLst(OpnRefAdicLstType value) {
        return new JAXBElement<OpnRefAdicLstType>(_OpnRefAdicLst_QNAME, OpnRefAdicLstType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "NroSec")
    public JAXBElement<Long> createNroSec(Long value) {
        return new JAXBElement<Long>(_NroSec_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SegRpta")
    public JAXBElement<String> createSegRpta(String value) {
        return new JAXBElement<String>(_SegRpta_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "FecIni")
    public JAXBElement<String> createFecIni(String value) {
        return new JAXBElement<String>(_FecIni_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SucursalCod")
    public JAXBElement<String> createSucursalCod(String value) {
        return new JAXBElement<String>(_SucursalCod_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MtoDocLstType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "MtoDocLst")
    public JAXBElement<MtoDocLstType> createMtoDocLst(MtoDocLstType value) {
        return new JAXBElement<MtoDocLstType>(_MtoDocLst_QNAME, MtoDocLstType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PasswordType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "Password")
    public JAXBElement<PasswordType> createPassword(PasswordType value) {
        return new JAXBElement<PasswordType>(_Password_QNAME, PasswordType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ServStatusCod")
    public JAXBElement<String> createServStatusCod(String value) {
        return new JAXBElement<String>(_ServStatusCod_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SvcEmpDocAdicLstType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcEmpDocAdicLst")
    public JAXBElement<SvcEmpDocAdicLstType> createSvcEmpDocAdicLst(SvcEmpDocAdicLstType value) {
        return new JAXBElement<SvcEmpDocAdicLstType>(_SvcEmpDocAdicLst_QNAME, SvcEmpDocAdicLstType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "FecEmision")
    public JAXBElement<String> createFecEmision(String value) {
        return new JAXBElement<String>(_FecEmision_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "NomCompleto")
    public JAXBElement<String> createNomCompleto(String value) {
        return new JAXBElement<String>(_NomCompleto_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClientAppType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ClientApp")
    public JAXBElement<ClientAppType> createClientApp(ClientAppType value) {
        return new JAXBElement<ClientAppType>(_ClientApp_QNAME, ClientAppType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SegSecreto")
    public JAXBElement<String> createSegSecreto(String value) {
        return new JAXBElement<String>(_SegSecreto_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SvcRecaudaTrnRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcRecaudaTrnRef")
    public JAXBElement<SvcRecaudaTrnRefType> createSvcRecaudaTrnRef(SvcRecaudaTrnRefType value) {
        return new JAXBElement<SvcRecaudaTrnRefType>(_SvcRecaudaTrnRef_QNAME, SvcRecaudaTrnRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SvcRecaudaTrnRegType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SvcRecaudaTrnReg")
    public JAXBElement<SvcRecaudaTrnRegType> createSvcRecaudaTrnReg(SvcRecaudaTrnRegType value) {
        return new JAXBElement<SvcRecaudaTrnRegType>(_SvcRecaudaTrnReg_QNAME, SvcRecaudaTrnRegType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "RqUID")
    public JAXBElement<String> createRqUID(String value) {
        return new JAXBElement<String>(_RqUID_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CtxRqHdrType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "CtxRqHdr")
    public JAXBElement<CtxRqHdrType> createCtxRqHdr(CtxRqHdrType value) {
        return new JAXBElement<CtxRqHdrType>(_CtxRqHdr_QNAME, CtxRqHdrType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CredencialRqHdrType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "CredencialRqHdr")
    public JAXBElement<CredencialRqHdrType> createCredencialRqHdr(CredencialRqHdrType value) {
        return new JAXBElement<CredencialRqHdrType>(_CredencialRqHdr_QNAME, CredencialRqHdrType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SegTokenCardType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "SegTokenCard")
    public JAXBElement<SegTokenCardType> createSegTokenCard(SegTokenCardType value) {
        return new JAXBElement<SegTokenCardType>(_SegTokenCard_QNAME, SegTokenCardType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MsjRqHdrType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "MsjRqHdr")
    public JAXBElement<MsjRqHdrType> createMsjRqHdr(MsjRqHdrType value) {
        return new JAXBElement<MsjRqHdrType>(_MsjRqHdr_QNAME, MsjRqHdrType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "Des")
    public JAXBElement<String> createDes(String value) {
        return new JAXBElement<String>(_Des_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClientCtxIdType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ClientCtxId")
    public JAXBElement<ClientCtxIdType> createClientCtxId(ClientCtxIdType value) {
        return new JAXBElement<ClientCtxIdType>(_ClientCtxId_QNAME, ClientCtxIdType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "Ctd")
    public JAXBElement<Long> createCtd(Long value) {
        return new JAXBElement<Long>(_Ctd_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ProdNro")
    public JAXBElement<String> createProdNro(String value) {
        return new JAXBElement<String>(_ProdNro_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RangoMtoType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "RangoMto")
    public JAXBElement<RangoMtoType> createRangoMto(RangoMtoType value) {
        return new JAXBElement<RangoMtoType>(_RangoMto_QNAME, RangoMtoType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TrnTpLstType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "TrnTpLst")
    public JAXBElement<TrnTpLstType> createTrnTpLst(TrnTpLstType value) {
        return new JAXBElement<TrnTpLstType>(_TrnTpLst_QNAME, TrnTpLstType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExtornoTpType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ExtornoTp")
    public JAXBElement<ExtornoTpType> createExtornoTp(ExtornoTpType value) {
        return new JAXBElement<ExtornoTpType>(_ExtornoTp_QNAME, ExtornoTpType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ProdEspAutzFlg")
    public JAXBElement<String> createProdEspAutzFlg(String value) {
        return new JAXBElement<String>(_ProdEspAutzFlg_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TrnTpType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "TrnTp")
    public JAXBElement<TrnTpType> createTrnTp(TrnTpType value) {
        return new JAXBElement<TrnTpType>(_TrnTp_QNAME, TrnTpType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StatusAdicLstType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "StatusAdicLst")
    public JAXBElement<StatusAdicLstType> createStatusAdicLst(StatusAdicLstType value) {
        return new JAXBElement<StatusAdicLstType>(_StatusAdicLst_QNAME, StatusAdicLstType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "LngPgn")
    public JAXBElement<Long> createLngPgn(Long value) {
        return new JAXBElement<Long>(_LngPgn_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClientCtxIdLstType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "ClientCtxIdLst")
    public JAXBElement<ClientCtxIdLstType> createClientCtxIdLst(ClientCtxIdLstType value) {
        return new JAXBElement<ClientCtxIdLstType>(_ClientCtxIdLst_QNAME, ClientCtxIdLstType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "OpnRef")
    public JAXBElement<String> createOpnRef(String value) {
        return new JAXBElement<String>(_OpnRef_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CtxRsHdrType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://mfxV2_0", name = "CtxRsHdr")
    public JAXBElement<CtxRsHdrType> createCtxRsHdr(CtxRsHdrType value) {
        return new JAXBElement<CtxRsHdrType>(_CtxRsHdr_QNAME, CtxRsHdrType.class, null, value);
    }

}
