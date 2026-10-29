package cl.bcs.plataforma.jobs;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.sql2o.Sql2o;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;

public class ProcesadorArchivosJob extends BcsJob{
	
	private final Log log = new Log("PLA.PROCESADOR_ARCHIVOS");
	
	@Override
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		go(super.getSql2o(),false);
		return true;
		
	}
	
	public ArrayList<Field> getTBACFVCDefinition() {
		ArrayList<Field> bla = new ArrayList<Field>();	
			
		bla.add(new Field("ACF_MERCADO","NVARCHAR(2)",0));
		bla.add(new Field("ACF_NEMO","NVARCHAR(10)",1));
		bla.add(new Field("ACF_SEC_VC","BIGINT",2));
		bla.add(new Field("ACF_DESCRIP_VC","NVARCHAR(70)",3));
		bla.add(new Field("ACF_FAC_ACC_ANT","DECIMAL(26,8)",4));
		bla.add(new Field("ACF_FAC_ACC_NUE","DECIMAL(26,8)",5));
		bla.add(new Field("ACF_FEC_ACT_VC","NVARCHAR(10)",6));
		bla.add(new Field("ACF_FEC_LIM","NVARCHAR(10)",7));
		bla.add(new Field("ACF_FEC_PAGO","NVARCHAR(10)",8));
		bla.add(new Field("ACF_NEMO_2","NVARCHAR(10)",9));
		bla.add(new Field("ACF_NUM_ACC_ANT","DECIMAL(26,8)",10));
		bla.add(new Field("ACF_NUM_ACC_DER","DECIMAL(26,8)",11));
		bla.add(new Field("ACF_NUM_ACC_NUE","DECIMAL(26,8)",12));
		bla.add(new Field("ACF_NUM_DIVDO","DECIMAL(26,8)",13));
		bla.add(new Field("ACF_PRE_ANT_VC","DECIMAL(26,8)",14));
		bla.add(new Field("ACF_PRE_EX_VC","DECIMAL(26,8)",15));
		bla.add(new Field("ACF_TIPO_VC","NVARCHAR(2)",16));
		bla.add(new Field("ACF_TIPO_DIV","NVARCHAR(1)",17));
		bla.add(new Field("ACF_VAL_ACC","DECIMAL(26,8)",18));
		bla.add(new Field("ACF_VAL_AN_DS","DECIMAL(26,8)",19));
		bla.add(new Field("ACF_VAL_EX_DS","DECIMAL(26,8)",20));
		bla.add(new Field("ACF_VAL_LIB_AJU","DECIMAL(26,8)",21));
		bla.add(new Field("ACF_VIG_NAA","NVARCHAR(1)",22));
		bla.add(new Field("ACF_VC_PROC","NVARCHAR(1)",23));
		bla.add(new Field("ACF_MONEDA","NVARCHAR(5)",24));
		bla.add(new Field("ACV_MERCADO","NVARCHAR(2)",25));
		bla.add(new Field("ACV_NEMO","NVARCHAR(10)",26));
		bla.add(new Field("ACV_SEC_VC","BIGINT",27));
		bla.add(new Field("ACV_INDICE","BIGINT",28));
		bla.add(new Field("ACV_HORA_MOD","NVARCHAR(40)",29));
		bla.add(new Field("ACV_DESCRIP_VC","NVARCHAR(70)",30));
		bla.add(new Field("ACV_FAC_ACC_ANT","DECIMAL(26,8)",31));
		bla.add(new Field("ACV_FAC_ACC_NUE","DECIMAL(26,8)",32));
		bla.add(new Field("ACV_FEC_ACT_VC","NVARCHAR(10)",33));
		bla.add(new Field("ACV_FEC_LIM","NVARCHAR(10)",34));
		bla.add(new Field("ACV_FEC_PAGO","NVARCHAR(10)",35));
		bla.add(new Field("ACV_NEMO_2","NVARCHAR(10)",36));
		bla.add(new Field("ACV_NUM_ACC_ANT","DECIMAL(26,8)",37));
		bla.add(new Field("ACV_NUM_ACC_DER","DECIMAL(26,8)",38));
		bla.add(new Field("ACV_NUM_ACC_NUE","DECIMAL(26,8)",39));
		bla.add(new Field("ACV_NUM_DIVDO","DECIMAL(26,8)",40));
		bla.add(new Field("ACV_PRE_ANT_VC","DECIMAL(26,8)",41));
		bla.add(new Field("ACV_PRE_EX_VC","DECIMAL(26,8)",42));
		bla.add(new Field("ACV_TIPO_VC","NVARCHAR(2)",43));
		bla.add(new Field("ACV_TIPO_DIV","NVARCHAR(1)",44));
		bla.add(new Field("ACV_VAL_ACC","DECIMAL(26,8)",45));
		bla.add(new Field("ACV_VAL_AN_DS","DECIMAL(26,8)",46));
		bla.add(new Field("ACV_VAL_EX_DS","DECIMAL(26,8)",47));
		bla.add(new Field("ACV_VAL_LIB_AJU","DECIMAL(26,8)",48));
		bla.add(new Field("ACV_VIG_NAA","NVARCHAR(1)",49));
		bla.add(new Field("ACV_VC_PROC","NVARCHAR(1)",50));
		bla.add(new Field("ACV_MONEDA","NVARCHAR(5)",51));
		bla.add(new Field("ACV_USUARIO_MOD","NVARCHAR(20)",52));
		bla.add(new Field("ACV_TERMINAL_MOD","NVARCHAR(10)",53));
		bla.add(new Field("ACV_TIPO_MOD","NVARCHAR(1)",54));

		
		return bla;
	}
	
	public ArrayList<Field> getTBUTEMISDefinition() {
		ArrayList<Field> bla = new ArrayList<Field>();	
			
		bla.add(new Field("EMISOR","NVARCHAR(40)",0));
		bla.add(new Field("COD_EMI","NVARCHAR(40)",1));
		bla.add(new Field("COD_SVS","NVARCHAR(40)",2));
		bla.add(new Field("IND_IIF","NVARCHAR(40)",3));
		bla.add(new Field("IND_IRF","NVARCHAR(40)",4));
		bla.add(new Field("RAZON_SOCIAL","NVARCHAR(40)",5));
		bla.add(new Field("TIP_ENT","NVARCHAR(40)",6));
		bla.add(new Field("RUT","NVARCHAR(40)",7));
		bla.add(new Field("CLA_RIE_IIF","NVARCHAR(40)",8));
		bla.add(new Field("AUTORIZA_PACTO","NVARCHAR(40)",9));
		bla.add(new Field("IND_FUNG","NVARCHAR(40)",10));
		
		return bla;
	}
	
	public ArrayList<Field> getVectorPreciosDefinition() {
		ArrayList<Field> bla = new ArrayList<Field>();	
			
		bla.add(new Field("FECHA","NVARCHAR(40)",0));
		bla.add(new Field("NEMO","NVARCHAR(40)",1));
		bla.add(new Field("SEC_LISTA","INTEGER",2));
		bla.add(new Field("TIPO","NVARCHAR(40)",3));
		bla.add(new Field("VALOR","DECIMAL(26,8)",4));
		bla.add(new Field("VALOR_95","DECIMAL(26,8)",5));
		bla.add(new Field("VALOR_98","DECIMAL(26,8)",6));
		bla.add(new Field("VALOR_01","DECIMAL(26,8)",7));
		bla.add(new Field("VALOR_02","DECIMAL(26,8)",8));
		bla.add(new Field("VALOR_03","DECIMAL(26,8)",9));
		bla.add(new Field("VALOR_04","DECIMAL(26,8)",10));
		bla.add(new Field("VALOR_05","DECIMAL(26,8)",11));
		bla.add(new Field("PRECIO_BASE","DECIMAL(26,8)",12));
		
		return bla;
	}

	public ArrayList<Field> getTBRFMIDefinition() {
		ArrayList<Field> bla = new ArrayList<Field>();
		
		bla.add(new Field("RF_NEMO","NVARCHAR(40)",0));
		bla.add(new Field("RF_COD_OFI","NVARCHAR(40)",1));
		bla.add(new Field("RF_COND_COD_OFI_001","NVARCHAR(40)",2));
		bla.add(new Field("RF_COND_COD_OFI_002","NVARCHAR(40)",3));
		bla.add(new Field("RF_COND_COD_OFI_003","NVARCHAR(40)",4));
		bla.add(new Field("RF_COND_COD_OFI_004","NVARCHAR(40)",5));
		bla.add(new Field("RF_COND_COD_OFI_005","NVARCHAR(40)",6));
		bla.add(new Field("RF_COND_COD_OFI_006","NVARCHAR(40)",7));
		bla.add(new Field("RF_COND_COD_OFI_007","NVARCHAR(40)",8));
		bla.add(new Field("RF_COND_COD_OFI_008","NVARCHAR(40)",9));
		bla.add(new Field("RF_COND_COD_OFI_009","NVARCHAR(40)",10));
		bla.add(new Field("RF_COND_COD_OFI_010","NVARCHAR(40)",11));
		bla.add(new Field("RF_COND_DIS_OFI","NVARCHAR(40)",12));
		bla.add(new Field("RF_DIAS_TR_SN_AC","DECIMAL(26,8)",13));
		bla.add(new Field("RF_DIAS_TR_ME_AC","DECIMAL(26,8)",14));
		bla.add(new Field("RF_EMI_PROV","NVARCHAR(40)",15));
		bla.add(new Field("RF_ESTADO","NVARCHAR(40)",16));
		bla.add(new Field("RF_FAC_FRACC_OFE","DECIMAL(26,8)",17));
		bla.add(new Field("RF_FEC_EMI","NVARCHAR(40)",18));
		bla.add(new Field("RF_FEC_FIJ_CIE","NVARCHAR(40)",19));
		bla.add(new Field("RF_FEC_VCTO","NVARCHAR(40)",20));
		bla.add(new Field("RF_IND_BOLSON","NVARCHAR(40)",21));
		bla.add(new Field("RF_HORA_FIJ_CIE","NVARCHAR(40)",22));
		bla.add(new Field("RF_MONTO","DECIMAL(26,8)",23));
		bla.add(new Field("RF_MONTO_SN_AC","DECIMAL(26,8)",24));
		bla.add(new Field("RF_MONTO_ME_AC","DECIMAL(26,8)",25));
		bla.add(new Field("RF_MONTO_MED_DI_AC","DECIMAL(26,8)",26));
		bla.add(new Field("RF_MONTO_MED_SN_AC","DECIMAL(26,8)",27));
		bla.add(new Field("RF_MONTO_X_PMED_DI","DECIMAL(26,8)",28));
		bla.add(new Field("RF_MONTO_X_PMED_SN","DECIMAL(26,8)",29));
		bla.add(new Field("RF_MONTO_X_TIR","DECIMAL(26,8)",30));
		bla.add(new Field("RF_RAZON_SOCIAL","NVARCHAR(40)",31));
		bla.add(new Field("RF_NUM_NEG","DECIMAL(26,8)",32));
		bla.add(new Field("RF_PRE_CIE_AN_AN","DECIMAL(26,8)",33));
		bla.add(new Field("RF_PRE_CIE_DI_AC","DECIMAL(26,8)",34));
		bla.add(new Field("RF_PRE_CIE_ME_AN","DECIMAL(26,8)",35));
		bla.add(new Field("RF_PRE_CIE_OFI","DECIMAL(26,8)",36));
		bla.add(new Field("RF_PRE_CIE_SN_AN","DECIMAL(26,8)",37));
		bla.add(new Field("RF_PRE_MAY","DECIMAL(26,8)",38));
		bla.add(new Field("RF_PRE_MAY_SN_AC","DECIMAL(26,8)",39));
		bla.add(new Field("RF_PRE_MED","DECIMAL(26,8)",40));
		bla.add(new Field("RF_PRE_MED_SN_AC","DECIMAL(26,8)",41));
		bla.add(new Field("RF_PRE_MEN","DECIMAL(26,8)",42));
		bla.add(new Field("RF_PRE_MEN_SN_AC","DECIMAL(26,8)",43));
		bla.add(new Field("RF_TENDEN","NVARCHAR(40)",44));
		bla.add(new Field("RF_TIPO_CALCULO","NVARCHAR(40)",45));
		bla.add(new Field("RF_TIR_MED","DECIMAL(26,8)",46));
		bla.add(new Field("RF_UNI_TRA","DECIMAL(26,8)",47));
		bla.add(new Field("RF_UNI_TRA_SN_AC","DECIMAL(26,8)",48));
		bla.add(new Field("RF_UNI_TRA_ME_AC","DECIMAL(26,8)",49));
		bla.add(new Field("RF_COD_SERIE","NVARCHAR(40)",50));
		bla.add(new Field("RF_FAMILIA","NVARCHAR(40)",51));
		bla.add(new Field("RF_GENERICO","NVARCHAR(40)",52));
		bla.add(new Field("RF_MONEDA","NVARCHAR(40)",53));
		bla.add(new Field("RF_TIR_MAY","DECIMAL(26,8)",54));
		bla.add(new Field("RF_TIR_MEN","DECIMAL(26,8)",55));
		bla.add(new Field("RF_DURATION_MED","DECIMAL(26,8)",56));
		bla.add(new Field("RF_MONTO_DURATION","DECIMAL(26,8)",57));
		bla.add(new Field("RF_CODIGO_ISIN","NVARCHAR(40)",58));
		bla.add(new Field("SE_COD_SERIE","NVARCHAR(40)",59));
		bla.add(new Field("SE_COD_BCO","NVARCHAR(40)",60));
		bla.add(new Field("SE_COD_OFI","NVARCHAR(40)",61));
		bla.add(new Field("SE_CORTE_MIN","DECIMAL(26,8)",62));
		bla.add(new Field("SE_CLASIF_RIESGO","NVARCHAR(40)",63));
		bla.add(new Field("SE_IND_DIVISI","NVARCHAR(40)",64));
		bla.add(new Field("SE_EMISOR","NVARCHAR(40)",65));
		bla.add(new Field("SE_FAC_FRACC_OFE","DECIMAL(26,8)",66));
		bla.add(new Field("SE_FAMILIA","NVARCHAR(40)",67));
		bla.add(new Field("SE_FEC_COM_PPAG","NVARCHAR(40)",68));
		bla.add(new Field("SE_FEC_PRI_EMI","NVARCHAR(40)",69));
		bla.add(new Field("SE_FEC_VCTO","NVARCHAR(40)",70));
		bla.add(new Field("SE_IND_BOLSON","NVARCHAR(40)",71));
		bla.add(new Field("SE_IRF_INSCRIP","NVARCHAR(40)",72));
		bla.add(new Field("SE_MONTO_EMI","DECIMAL(26,8)",73));
		bla.add(new Field("SE_NUM_AMORT","DECIMAL(26,8)",74));
		bla.add(new Field("SE_NUM_CAR_VAL","DECIMAL(26,8)",75));
		bla.add(new Field("SE_NUM_CUPON","DECIMAL(26,8)",76));
		bla.add(new Field("SE_ORDEN","DECIMAL(26,8)",77));
		bla.add(new Field("SE_PERI_PAGO_CUPON","DECIMAL(26,8)",78));
		bla.add(new Field("SE_PLAZO","DECIMAL(26,8)",79));
		bla.add(new Field("SE_RANGO_PLAZO","NVARCHAR(40)",80));
		bla.add(new Field("SE_RANGO_TASA","NVARCHAR(40)",81));
		bla.add(new Field("SE_TASA_NOMI_EMI","DECIMAL(26,8)",82));
		bla.add(new Field("SE_TERA","DECIMAL(26,8)",83));
		bla.add(new Field("SE_TIPO_AMORT","NVARCHAR(40)",84));
		bla.add(new Field("SE_TIPO_REAJ","NVARCHAR(40)",85));
		bla.add(new Field("SE_TIPO_INSTRU","NVARCHAR(40)",86));
		bla.add(new Field("SE_TIPO_INTERES","NVARCHAR(40)",87));
		bla.add(new Field("SE_TIPO_NOMINAL","NVARCHAR(40)",88));
		bla.add(new Field("SE_COMENTARIOS","NVARCHAR(40)",89));
		bla.add(new Field("SE_FEC_INGRESO","NVARCHAR(40)",90));
		bla.add(new Field("SE_MONEDA","NVARCHAR(40)",91));
		bla.add(new Field("SE_ORDEN_CLASIF_RIESGO","DECIMAL(26,8)",92));
		bla.add(new Field("SE_TIPO_MONEDA_PAGO","NVARCHAR(40)",93));
		bla.add(new Field("SE_TASA_FISCAL","DECIMAL(26,8)",94));
		bla.add(new Field("SE_IND_VENTA_CORTA","NVARCHAR(40)",95));
		bla.add(new Field("SE_IND_ART104","NVARCHAR(40)",96));


		
		return bla;
	}

	public ArrayList<Field> getTBMOMIDefinition() {
		ArrayList<Field> bla = new ArrayList<Field>();
		
		bla.add(new Field("NEMO","NVARCHAR(40)",0));
		bla.add(new Field("LOTE_PADRON","DECIMAL(268)",1));
		bla.add(new Field("COD_OFI","NVARCHAR(40)",2));
		bla.add(new Field("COND_COD_DI_AC_001","NVARCHAR(40)",3));
		bla.add(new Field("COND_COD_DI_AC_002","NVARCHAR(40)",4));
		bla.add(new Field("COND_COD_DI_AC_003","NVARCHAR(40)",5));
		bla.add(new Field("COND_COD_DI_AC_004","NVARCHAR(40)",6));
		bla.add(new Field("COND_COD_DI_AC_005","NVARCHAR(40)",7));
		bla.add(new Field("COND_COD_DI_AC_006","NVARCHAR(40)",8));
		bla.add(new Field("COND_COD_DI_AC_007","NVARCHAR(40)",9));
		bla.add(new Field("COND_COD_DI_AC_008","NVARCHAR(40)",10));
		bla.add(new Field("COND_COD_DI_AC_009","NVARCHAR(40)",11));
		bla.add(new Field("COND_COD_DI_AC_010","NVARCHAR(40)",12));
		bla.add(new Field("COND_COD_OFI_001","NVARCHAR(40)",13));
		bla.add(new Field("COND_COD_OFI_002","NVARCHAR(40)",14));
		bla.add(new Field("COND_COD_OFI_003","NVARCHAR(40)",15));
		bla.add(new Field("COND_COD_OFI_004","NVARCHAR(40)",16));
		bla.add(new Field("COND_COD_OFI_005","NVARCHAR(40)",17));
		bla.add(new Field("COND_COD_OFI_006","NVARCHAR(40)",18));
		bla.add(new Field("COND_COD_OFI_007","NVARCHAR(40)",19));
		bla.add(new Field("COND_COD_OFI_008","NVARCHAR(40)",20));
		bla.add(new Field("COND_COD_OFI_009","NVARCHAR(40)",21));
		bla.add(new Field("COND_COD_OFI_010","NVARCHAR(40)",22));
		bla.add(new Field("COND_DIS_DI_AC","NVARCHAR(40)",23));
		bla.add(new Field("COND_DIS_DI_AN","NVARCHAR(40)",24));
		bla.add(new Field("COND_DIS_OFI","NVARCHAR(40)",25));
		bla.add(new Field("ESTADO","NVARCHAR(40)",26));
		bla.add(new Field("FEC_FIJ_CIE","NVARCHAR(40)",27));
		bla.add(new Field("FIJA_CIE","NVARCHAR(40)",28));
		bla.add(new Field("HORA_FIJ_CIE","NVARCHAR(40)",29));
		bla.add(new Field("MONTO","DECIMAL(268)",30));
		bla.add(new Field("MONTO_SN_AC","DECIMAL(268)",31));
		bla.add(new Field("MONTO_ME_AC","DECIMAL(268)",32));
		bla.add(new Field("RAZON_SOCIAL","NVARCHAR(40)",33));
		bla.add(new Field("NUM_DEC_PRE","DECIMAL(268)",34));
		bla.add(new Field("NUM_NEG","DECIMAL(268)",35));
		bla.add(new Field("NUM_REDON","DECIMAL(268)",36));
		bla.add(new Field("PRE_CIE_DI_AC","DECIMAL(268)",37));
		bla.add(new Field("PRE_CIE_DI_AN","DECIMAL(268)",38));
		bla.add(new Field("PRE_CIE_OFI","DECIMAL(268)",39));
		bla.add(new Field("PRE_MAY","DECIMAL(268)",40));
		bla.add(new Field("PRE_MED","DECIMAL(268)",41));
		bla.add(new Field("PRE_MEN","DECIMAL(268)",42));
		bla.add(new Field("TENDEN_DI_AC","NVARCHAR(40)",43));
		bla.add(new Field("TIPO_INSTRU","NVARCHAR(40)",44));
		bla.add(new Field("PRE_ULT_DI_AC","DECIMAL(268)",45));
		bla.add(new Field("RENT_AN_AC","DECIMAL(268)",46));
		bla.add(new Field("RENT_AN_AN","DECIMAL(268)",47));
		bla.add(new Field("UNI_TRA","DECIMAL(268)",48));
		bla.add(new Field("UNI_TRA_SN_AC","DECIMAL(268)",49));
		bla.add(new Field("UNI_TRA_ME_AC","DECIMAL(268)",50));
		
		return bla;
	}
	
	public ArrayList<Field> getTBFUMIDefinition() {
		ArrayList<Field> bla = new ArrayList<Field>();
		bla.add(new Field("NEMO","NVARCHAR(40)",0));
		bla.add(new Field("LOTE_PADRON","DECIMAL(268)",1));
		bla.add(new Field("COD_OFI","NVARCHAR(40)",2));
		bla.add(new Field("COND_COD_DI_AC_001","NVARCHAR(40)",3));
		bla.add(new Field("COND_COD_DI_AC_002","NVARCHAR(40)",4));
		bla.add(new Field("COND_COD_DI_AC_003","NVARCHAR(40)",5));
		bla.add(new Field("COND_COD_DI_AC_004","NVARCHAR(40)",6));
		bla.add(new Field("COND_COD_DI_AC_005","NVARCHAR(40)",7));
		bla.add(new Field("COND_COD_DI_AC_006","NVARCHAR(40)",8));
		bla.add(new Field("COND_COD_DI_AC_007","NVARCHAR(40)",9));
		bla.add(new Field("COND_COD_DI_AC_008","NVARCHAR(40)",10));
		bla.add(new Field("COND_COD_DI_AC_009","NVARCHAR(40)",11));
		bla.add(new Field("COND_COD_DI_AC_010","NVARCHAR(40)",12));
		bla.add(new Field("COND_COD_OFI_001","NVARCHAR(40)",13));
		bla.add(new Field("COND_COD_OFI_002","NVARCHAR(40)",14));
		bla.add(new Field("COND_COD_OFI_003","NVARCHAR(40)",15));
		bla.add(new Field("COND_COD_OFI_004","NVARCHAR(40)",16));
		bla.add(new Field("COND_COD_OFI_005","NVARCHAR(40)",17));
		bla.add(new Field("COND_COD_OFI_006","NVARCHAR(40)",18));
		bla.add(new Field("COND_COD_OFI_007","NVARCHAR(40)",19));
		bla.add(new Field("COND_COD_OFI_008","NVARCHAR(40)",20));
		bla.add(new Field("COND_COD_OFI_009","NVARCHAR(40)",21));
		bla.add(new Field("COND_COD_OFI_010","NVARCHAR(40)",22));
		bla.add(new Field("COND_DIS_DI_AN","NVARCHAR(40)",23));
		bla.add(new Field("COND_DIS_DI_AC","NVARCHAR(40)",24));
		bla.add(new Field("COND_DIS_OFI","NVARCHAR(40)",25));
		bla.add(new Field("DIAS_AL_VCTO","DECIMAL(268)",26));
		bla.add(new Field("ESTADO","NVARCHAR(40)",27));
		bla.add(new Field("FEC_FIJ_CIE","NVARCHAR(40)",28));
		bla.add(new Field("FEC_INI_VIG","NVARCHAR(40)",29));
		bla.add(new Field("FEC_VCTO","NVARCHAR(40)",30));
		bla.add(new Field("HORA_FIJ_CIE","NVARCHAR(40)",31));
		bla.add(new Field("INT_ABI","DECIMAL(268)",32));
		bla.add(new Field("MAR_INI","DECIMAL(268)",33));
		bla.add(new Field("MONTO_ACU_10","DECIMAL(268)",34));
		bla.add(new Field("MONTO","DECIMAL(268)",35));
		bla.add(new Field("RAZON_SOCIAL","NVARCHAR(40)",36));
		bla.add(new Field("NUM_DEC_PRE","DECIMAL(268)",37));
		bla.add(new Field("NUM_CON","DECIMAL(268)",38));
		bla.add(new Field("NUM_NEG","DECIMAL(268)",39));
		bla.add(new Field("NUM_REDON","DECIMAL(268)",40));
		bla.add(new Field("PTJE_REST_PRE","DECIMAL(268)",41));
		bla.add(new Field("PRE_CIE_DI_AC","DECIMAL(268)",42));
		bla.add(new Field("PRE_CIE_DI_AN","DECIMAL(268)",43));
		bla.add(new Field("PRE_CIE_OFI","DECIMAL(268)",44));
		bla.add(new Field("PRE_MAY","DECIMAL(268)",45));
		bla.add(new Field("PRE_MED","DECIMAL(268)",46));
		bla.add(new Field("PRE_MEN","DECIMAL(268)",47));
		bla.add(new Field("PRE_PRO_10","DECIMAL(268)",48));
		bla.add(new Field("TASA_NOMI","DECIMAL(268)",49));
		bla.add(new Field("TENDEN_DI_AC","NVARCHAR(40)",50));
		bla.add(new Field("TIPO_INSTRU","NVARCHAR(40)",51));
		bla.add(new Field("PRE_PEN_DI_AC","DECIMAL(268)",52));
		bla.add(new Field("PRE_ULT_DI_AC","DECIMAL(268)",53));
		bla.add(new Field("VAL_ACT","DECIMAL(268)",54));
		bla.add(new Field("VAR_PRE_CTDO","DECIMAL(268)",55));
		bla.add(new Field("VAL_CONTRATO_BCS","DECIMAL(268)",56));
		bla.add(new Field("CATEGORIA","NVARCHAR(40)",57));
		bla.add(new Field("PRE_APE_DI_AC","DECIMAL(268)",58));
		bla.add(new Field("ULT_DIA_NEG","NVARCHAR(40)",59));
		bla.add(new Field("VAL_CONTRATO_UNI","DECIMAL(268)",60));		
		return bla;
	}
	
	public ArrayList<Field> getTBACMIDefinition() {
		ArrayList<Field> bla = new ArrayList<Field>();
		
		bla.add(new Field("RV_MERCADO","NVARCHAR(40)",0));
		bla.add(new Field("RV_NEMO","NVARCHAR(40)",1));
		bla.add(new Field("RV_PRE_PULT_TR","DECIMAL(268)",2));
		bla.add(new Field("RV_PRE_ULT_TR","DECIMAL(268)",3));
		bla.add(new Field("RV_CATEGORIA","DECIMAL(268)",4));
		bla.add(new Field("RV_IND_CARTERA","NVARCHAR(40)",5));
		bla.add(new Field("RV_IND_EXCLUIDA","NVARCHAR(40)",6));
		bla.add(new Field("RV_AUT_VT_CORTO","NVARCHAR(40)",7));
		bla.add(new Field("RV_TENDEN_VT_CORTO","NVARCHAR(40)",8));
		bla.add(new Field("RV_IND_LIQ_DCV","NVARCHAR(40)",9));
		bla.add(new Field("RV_BOLSA","NVARCHAR(40)",10));
		bla.add(new Field("RV_MONEDA","DECIMAL(268)",11));
		bla.add(new Field("RV_AUT_OD_BLOQUE","NVARCHAR(40)",12));
		bla.add(new Field("RV_COD_RUBRO","NVARCHAR(40)",13));
		bla.add(new Field("RV_PRE_APE_DI_AC","DECIMAL(268)",14));
		bla.add(new Field("RV_PRE_MAY_52SEM","DECIMAL(268)",15));
		bla.add(new Field("RV_PRE_MEN_52SEM","DECIMAL(268)",16));
		bla.add(new Field("RV_PRE_CIE_7DIAS","DECIMAL(268)",17));
		bla.add(new Field("RV_PRE_CIE_30DIAS","DECIMAL(268)",18));
		bla.add(new Field("RV_PRE_CIE_52SEM","DECIMAL(268)",19));
		bla.add(new Field("RV_RENT_7DIAS","DECIMAL(268)",20));
		bla.add(new Field("RV_RENT_30DIAS","DECIMAL(268)",21));
		bla.add(new Field("RV_RENT_52SEM","DECIMAL(268)",22));
		bla.add(new Field("RV_DIVIDEND_YIELD","DECIMAL(268)",23));
		bla.add(new Field("RV_BETA_90DIAS","DECIMAL(268)",24));
		bla.add(new Field("RV_CODIGO_ISIN","NVARCHAR(40)",25));
		bla.add(new Field("RV_P_IPSA","DECIMAL(268)",26));
		bla.add(new Field("RV_P_IGPA","DECIMAL(268)",27));
		bla.add(new Field("RV_P_INTER_10","DECIMAL(268)",28));
		bla.add(new Field("RV_ACCIONES_EQ","DECIMAL(268)",29));
		bla.add(new Field("RV_VALOR_EQ","DECIMAL(268)",30));
		bla.add(new Field("RV_NON_FFLOAT","DECIMAL(268)",31));
		bla.add(new Field("RV_SECTOR","NVARCHAR(40)",32));
		bla.add(new Field("RV_PRE_CIE_TEORICO","DECIMAL(268)",33));
		bla.add(new Field("RV_BOLSA_EX","NVARCHAR(40)",34));
		bla.add(new Field("RV_MONEDA_EX","NVARCHAR(40)",35));
		bla.add(new Field("RV_PAIS_EMISOR","NVARCHAR(40)",36));
		bla.add(new Field("RV_TIPO_INSTRU","NVARCHAR(40)",37));
		bla.add(new Field("RV_VOL_52SEM","DECIMAL(268)",38));
		bla.add(new Field("RV_LISTA","NVARCHAR(40)",39));
		bla.add(new Field("AC_ACC_INSCRIP","DECIMAL(268)",40));
		bla.add(new Field("AC_AUT_OPE_SIMUL","NVARCHAR(40)",41));
		bla.add(new Field("AC_AUT_TRAN_PLAZO","NVARCHAR(40)",42));
		bla.add(new Field("AC_LOTE_PADRON","DECIMAL(268)",43));
		bla.add(new Field("AC_CESA_PAGO","NVARCHAR(40)",44));
		bla.add(new Field("AC_CLASE","NVARCHAR(40)",45));
		bla.add(new Field("AC_COD_FONOB","NVARCHAR(40)",46));
		bla.add(new Field("AC_COD_IGPA","NVARCHAR(40)",47));
		bla.add(new Field("AC_COD_OFI","NVARCHAR(40)",48));
		bla.add(new Field("AC_COD_OFI_SASA","NVARCHAR(40)",49));
		bla.add(new Field("AC_COLUMNA","DECIMAL(268)",50));
		bla.add(new Field("AC_COND_COD_DI_AC_001","NVARCHAR(40)",51));
		bla.add(new Field("AC_COND_COD_DI_AC_002","NVARCHAR(40)",52));
		bla.add(new Field("AC_COND_COD_DI_AC_003","NVARCHAR(40)",53));
		bla.add(new Field("AC_COND_COD_DI_AC_004","NVARCHAR(40)",54));
		bla.add(new Field("AC_COND_COD_DI_AC_005","NVARCHAR(40)",55));
		bla.add(new Field("AC_COND_COD_DI_AC_006","NVARCHAR(40)",56));
		bla.add(new Field("AC_COND_COD_DI_AC_007","NVARCHAR(40)",57));
		bla.add(new Field("AC_COND_COD_DI_AC_008","NVARCHAR(40)",58));
		bla.add(new Field("AC_COND_COD_DI_AC_009","NVARCHAR(40)",59));
		bla.add(new Field("AC_COND_COD_DI_AC_010","NVARCHAR(40)",60));
		bla.add(new Field("AC_COND_COD_OFI_001","NVARCHAR(40)",61));
		bla.add(new Field("AC_COND_COD_OFI_002","NVARCHAR(40)",62));
		bla.add(new Field("AC_COND_COD_OFI_003","NVARCHAR(40)",63));
		bla.add(new Field("AC_COND_COD_OFI_004","NVARCHAR(40)",64));
		bla.add(new Field("AC_COND_COD_OFI_005","NVARCHAR(40)",65));
		bla.add(new Field("AC_COND_COD_OFI_006","NVARCHAR(40)",66));
		bla.add(new Field("AC_COND_COD_OFI_007","NVARCHAR(40)",67));
		bla.add(new Field("AC_COND_COD_OFI_008","NVARCHAR(40)",68));
		bla.add(new Field("AC_COND_COD_OFI_009","NVARCHAR(40)",69));
		bla.add(new Field("AC_COND_COD_OFI_010","NVARCHAR(40)",70));
		bla.add(new Field("AC_COND_DIS_AN_AN","NVARCHAR(40)",71));
		bla.add(new Field("AC_COND_DIS_DI_AN","NVARCHAR(40)",72));
		bla.add(new Field("AC_COND_DIS_DI_AC","NVARCHAR(40)",73));
		bla.add(new Field("AC_COND_DIS_ME_AN","NVARCHAR(40)",74));
		bla.add(new Field("AC_COND_DIS_OFI","NVARCHAR(40)",75));
		bla.add(new Field("AC_COND_DIS_SE_AN","NVARCHAR(40)",76));
		bla.add(new Field("AC_COND_DIS_SN_AN","NVARCHAR(40)",77));
		bla.add(new Field("AC_DIAS_TR_ME_AC","DECIMAL(268)",78));
		bla.add(new Field("AC_DIAS_TR_SE_AC","DECIMAL(268)",79));
		bla.add(new Field("AC_DIAS_TR_AN_AC","DECIMAL(268)",80));
		bla.add(new Field("AC_DIAS_TR_TR_AC","DECIMAL(268)",81));
		bla.add(new Field("AC_DER_ESP","NVARCHAR(40)",82));
		bla.add(new Field("AC_DIVIDENDOS","DECIMAL(268)",83));
		bla.add(new Field("AC_ESTADO","NVARCHAR(40)",84));
		bla.add(new Field("AC_ESTADTRI","NVARCHAR(40)",85));
		bla.add(new Field("AC_FAC_IGPA","DECIMAL(268)",86));
		bla.add(new Field("AC_FAC_IGPA_FBASE","DECIMAL(268)",87));
		bla.add(new Field("AC_FAC_IPSA","DECIMAL(268)",88));
		bla.add(new Field("AC_FEC_EST_SIT","NVARCHAR(40)",89));
		bla.add(new Field("AC_FEC_FIJ_CIE","NVARCHAR(40)",90));
		bla.add(new Field("AC_FEC_INSCRIP","NVARCHAR(40)",91));
		bla.add(new Field("AC_FEC_ULT_TR","NVARCHAR(40)",92));
		bla.add(new Field("AC_FILA","DECIMAL(268)",93));
		bla.add(new Field("AC_IND_AJU_VC_ULT_TR","NVARCHAR(40)",94));
		bla.add(new Field("AC_IND_PIZ_ELEC","NVARCHAR(40)",95));
		bla.add(new Field("AC_IND_SIT_ESP","NVARCHAR(40)",96));
		bla.add(new Field("AC_INVER_ME","DECIMAL(268)",97));
		bla.add(new Field("AC_HORA_FIJ_CIE","NVARCHAR(40)",98));
		bla.add(new Field("AC_INVER_SE","DECIMAL(268)",99));
		bla.add(new Field("AC_INVER_AN","DECIMAL(268)",100));
		bla.add(new Field("AC_LIQUIDA","NVARCHAR(40)",101));
		bla.add(new Field("AC_MONTO_AN_AC","DECIMAL(268)",102));
		bla.add(new Field("AC_MONTO_AN_AN","DECIMAL(268)",103));
		bla.add(new Field("AC_MONTO_DI_AC","DECIMAL(268)",104));
		bla.add(new Field("AC_MONTO_DI_AN","DECIMAL(268)",105));
		bla.add(new Field("AC_MONTO_ME_AC","DECIMAL(268)",106));
		bla.add(new Field("AC_MONTO_ME_AN","DECIMAL(268)",107));
		bla.add(new Field("AC_MONTO_PMED_DI_AC","DECIMAL(268)",108));
		bla.add(new Field("AC_MONTO_PMED_SN_AC","DECIMAL(268)",109));
		bla.add(new Field("AC_MONTO_PMED_ME_AC","DECIMAL(268)",110));
		bla.add(new Field("AC_MONTO_PMED_SE_AC","DECIMAL(268)",111));
		bla.add(new Field("AC_MONTO_PMED_AN_AC","DECIMAL(268)",112));
		bla.add(new Field("AC_MONTO_PROM_UF_3M","DECIMAL(268)",113));
		bla.add(new Field("AC_MONTO_SE_AC","DECIMAL(268)",114));
		bla.add(new Field("AC_MONTO_SE_AN","DECIMAL(268)",115));
		bla.add(new Field("AC_MONTO_SN_AC","DECIMAL(268)",116));
		bla.add(new Field("AC_MONTO_SN_AN","DECIMAL(268)",117));
		bla.add(new Field("AC_NAA","DECIMAL(268)",118));
		bla.add(new Field("AC_NEMO_SASA","NVARCHAR(40)",119));
		bla.add(new Field("AC_RAZON_SOCIAL","NVARCHAR(40)",120));
		bla.add(new Field("AC_NUM_DEC_PRE","DECIMAL(268)",121));
		bla.add(new Field("AC_NUM_ACC_CIR","DECIMAL(268)",122));
		bla.add(new Field("AC_NUM_ACC_31DIC","DECIMAL(268)",123));
		bla.add(new Field("AC_NUM_MEN_FONOB","NVARCHAR(40)",124));
		bla.add(new Field("AC_NUM_MES_UTI","DECIMAL(268)",125));
		bla.add(new Field("AC_NUM_NEG_AN_AC","DECIMAL(268)",126));
		bla.add(new Field("AC_NUM_NEG_AN_AN","DECIMAL(268)",127));
		bla.add(new Field("AC_NUM_NEG_DI_AC","DECIMAL(268)",128));
		bla.add(new Field("AC_NUM_NEG_DI_AN","DECIMAL(268)",129));
		bla.add(new Field("AC_NUM_NEG_ME_AC","DECIMAL(268)",130));
		bla.add(new Field("AC_NUM_NEG_ME_AN","DECIMAL(268)",131));
		bla.add(new Field("AC_NUM_NEG_SE_AC","DECIMAL(268)",132));
		bla.add(new Field("AC_NUM_NEG_SE_AN","DECIMAL(268)",133));
		bla.add(new Field("AC_NUM_NEG_SN_AC","DECIMAL(268)",134));
		bla.add(new Field("AC_NUM_NEG_SN_AN","DECIMAL(268)",135));
		bla.add(new Field("AC_NUM_REDON","DECIMAL(268)",136));
		bla.add(new Field("AC_PATRI_AJU","DECIMAL(268)",137));
		bla.add(new Field("AC_PATRI_CON","DECIMAL(268)",138));
		bla.add(new Field("AC_PATRI_AN_AC","DECIMAL(268)",139));
		bla.add(new Field("AC_PATRI_AN_AN","DECIMAL(268)",140));
		bla.add(new Field("AC_PATRI_BASE","DECIMAL(268)",141));
		bla.add(new Field("AC_PATRI_ME_AC","DECIMAL(268)",142));
		bla.add(new Field("AC_PATRI_ME_AN","DECIMAL(268)",143));
		bla.add(new Field("AC_PATRI_NEG","NVARCHAR(40)",144));
		bla.add(new Field("AC_PATRI_SE_AC","DECIMAL(268)",145));
		bla.add(new Field("AC_PATRI_SE_AN","DECIMAL(268)",146));
		bla.add(new Field("AC_PRESEN_AN_AC","DECIMAL(268)",147));
		bla.add(new Field("AC_PRESEN_AN_AN","DECIMAL(268)",148));
		bla.add(new Field("AC_PRESEN_ME_AC","DECIMAL(268)",149));
		bla.add(new Field("AC_PRESEN_ME_AN","DECIMAL(268)",150));
		bla.add(new Field("AC_PRESEN_SE_AC","DECIMAL(268)",151));
		bla.add(new Field("AC_PRESEN_SE_AN","DECIMAL(268)",152));
		bla.add(new Field("AC_PRESEN_PRO_12_M","DECIMAL(268)",153));
		bla.add(new Field("AC_PRESEN_PRO_1_T","DECIMAL(268)",154));
		bla.add(new Field("AC_PRESEN_PRO_2_T","DECIMAL(268)",155));
		bla.add(new Field("AC_PRESEN_PRO_3_T","DECIMAL(268)",156));
		bla.add(new Field("AC_PRESEN_PRO_4_T","DECIMAL(268)",157));
		bla.add(new Field("AC_PRE_CIE_AN_AN","DECIMAL(268)",158));
		bla.add(new Field("AC_PRE_CIE_DI_AC","DECIMAL(268)",159));
		bla.add(new Field("AC_PRE_CIE_DI_AN","DECIMAL(268)",160));
		bla.add(new Field("AC_PRE_CIE_ME_AN","DECIMAL(268)",161));
		bla.add(new Field("AC_PRE_CIE_OFI","DECIMAL(268)",162));
		bla.add(new Field("AC_PRE_CIE_PEN","DECIMAL(268)",163));
		bla.add(new Field("AC_PRE_CIE_SE_AN","DECIMAL(268)",164));
		bla.add(new Field("AC_PRE_CIE_SN_AN","DECIMAL(268)",165));
		bla.add(new Field("AC_PRE_CIE_BASE","DECIMAL(268)",166));
		bla.add(new Field("AC_PRE_MAY_AN_AC","DECIMAL(268)",167));
		bla.add(new Field("AC_PRE_MAY_AN_AN","DECIMAL(268)",168));
		bla.add(new Field("AC_PRE_MAY_DI_AC","DECIMAL(268)",169));
		bla.add(new Field("AC_PRE_MAY_DI_AN","DECIMAL(268)",170));
		bla.add(new Field("AC_PRE_MAY_ME_AC","DECIMAL(268)",171));
		bla.add(new Field("AC_PRE_MAY_ME_AN","DECIMAL(268)",172));
		bla.add(new Field("AC_PRE_MAY_SE_AC","DECIMAL(268)",173));
		bla.add(new Field("AC_PRE_MAY_SE_AN","DECIMAL(268)",174));
		bla.add(new Field("AC_PRE_MAY_SN_AC","DECIMAL(268)",175));
		bla.add(new Field("AC_PRE_MAY_SN_AN","DECIMAL(268)",176));
		bla.add(new Field("AC_PRE_MED_AN_AC","DECIMAL(268)",177));
		bla.add(new Field("AC_PRE_MED_AN_AN","DECIMAL(268)",178));
		bla.add(new Field("AC_PRE_MED_DI_AC","DECIMAL(268)",179));
		bla.add(new Field("AC_PRE_MED_DI_AN","DECIMAL(268)",180));
		bla.add(new Field("AC_PRE_MED_ME_AC","DECIMAL(268)",181));
		bla.add(new Field("AC_PRE_MED_ME_AN","DECIMAL(268)",182));
		bla.add(new Field("AC_PRE_MED_SE_AC","DECIMAL(268)",183));
		bla.add(new Field("AC_PRE_MED_SE_AN","DECIMAL(268)",184));
		bla.add(new Field("AC_PRE_MED_SN_AC","DECIMAL(268)",185));
		bla.add(new Field("AC_PRE_MED_SN_AN","DECIMAL(268)",186));
		bla.add(new Field("AC_PRE_MED_ULT_TR","DECIMAL(268)",187));
		bla.add(new Field("AC_PRE_MEN_AN_AC","DECIMAL(268)",188));
		bla.add(new Field("AC_PRE_MEN_AN_AN","DECIMAL(268)",189));
		bla.add(new Field("AC_PRE_MEN_DI_AC","DECIMAL(268)",190));
		bla.add(new Field("AC_PRE_MEN_DI_AN","DECIMAL(268)",191));
		bla.add(new Field("AC_PRE_MEN_ME_AC","DECIMAL(268)",192));
		bla.add(new Field("AC_PRE_MEN_ME_AN","DECIMAL(268)",193));
		bla.add(new Field("AC_PRE_MEN_SE_AC","DECIMAL(268)",194));
		bla.add(new Field("AC_PRE_MEN_SE_AN","DECIMAL(268)",195));
		bla.add(new Field("AC_PRE_MEN_SN_AC","DECIMAL(268)",196));
		bla.add(new Field("AC_PRE_MEN_SN_AN","DECIMAL(268)",197));
		bla.add(new Field("AC_REL_BOL_LIB","DECIMAL(268)",198));
		bla.add(new Field("AC_REL_PRE_DIV","DECIMAL(268)",199));
		bla.add(new Field("AC_REL_PRE_UTI","DECIMAL(268)",200));
		bla.add(new Field("AC_RENT_AN_AC","DECIMAL(268)",201));
		bla.add(new Field("AC_RENT_AN_AN","DECIMAL(268)",202));
		bla.add(new Field("AC_RENT_ME_AC","DECIMAL(268)",203));
		bla.add(new Field("AC_RENT_ME_AN","DECIMAL(268)",204));
		bla.add(new Field("AC_RENT_SE_AC","DECIMAL(268)",205));
		bla.add(new Field("AC_RENT_SE_AN","DECIMAL(268)",206));
		bla.add(new Field("AC_TENDEN_DI_AC","NVARCHAR(40)",207));
		bla.add(new Field("AC_TIPO_ENT","NVARCHAR(40)",208));
		bla.add(new Field("AC_PRE_ULT_DI_AC","DECIMAL(268)",209));
		bla.add(new Field("AC_UNI_PMED_DI_AC","DECIMAL(268)",210));
		bla.add(new Field("AC_UNI_PMED_SN_AC","DECIMAL(268)",211));
		bla.add(new Field("AC_UNI_PMED_ME_AC","DECIMAL(268)",212));
		bla.add(new Field("AC_UNI_PMED_SE_AC","DECIMAL(268)",213));
		bla.add(new Field("AC_UNI_PMED_AN_AC","DECIMAL(268)",214));
		bla.add(new Field("AC_UNI_TRA_AN_AC","DECIMAL(268)",215));
		bla.add(new Field("AC_UNI_TRA_AN_AN","DECIMAL(268)",216));
		bla.add(new Field("AC_UNI_TRA_DI_AC","DECIMAL(268)",217));
		bla.add(new Field("AC_UNI_TRA_DI_AN","DECIMAL(268)",218));
		bla.add(new Field("AC_UNI_TRA_ME_AC","DECIMAL(268)",219));
		bla.add(new Field("AC_UNI_TRA_ME_AN","DECIMAL(268)",220));
		bla.add(new Field("AC_UNI_TRA_SE_AC","DECIMAL(268)",221));
		bla.add(new Field("AC_UNI_TRA_SE_AN","DECIMAL(268)",222));
		bla.add(new Field("AC_UNI_TRA_SN_AC","DECIMAL(268)",223));
		bla.add(new Field("AC_UNI_TRA_SN_AN","DECIMAL(268)",224));
		bla.add(new Field("AC_UNI_TRA_ULT_TR","DECIMAL(268)",225));
		bla.add(new Field("AC_UTI_12_M","DECIMAL(268)",226));
		bla.add(new Field("AC_UTI_NUM_MES","DECIMAL(268)",227));
		bla.add(new Field("AC_VAR_PRE","DECIMAL(268)",228));
		bla.add(new Field("SOC_RUT","NVARCHAR(40)",229));
		bla.add(new Field("EMI_EMISOR","NVARCHAR(40)",230));
		bla.add(new Field("EMI_COD_EMI","NVARCHAR(40)",231));
		bla.add(new Field("EMI_COD_SVS","NVARCHAR(40)",232));
		bla.add(new Field("EMI_IND_IIF","NVARCHAR(40)",233));
		bla.add(new Field("EMI_IND_IRF","NVARCHAR(40)",234));
		bla.add(new Field("EMI_RAZON_SOCIAL","NVARCHAR(40)",235));
		bla.add(new Field("EMI_TIP_ENT","NVARCHAR(40)",236));
		bla.add(new Field("EMI_RUT","NVARCHAR(40)",237));
		bla.add(new Field("EMI_CLA_RIE_IIF","NVARCHAR(40)",238));
		bla.add(new Field("EMI_AUTORIZA_PACTO","NVARCHAR(40)",239));
		bla.add(new Field("EMI_IND_FUNG","NVARCHAR(40)",240));
		
		return bla;
	}
	
	public ArrayList<Field> getTBCFMIDefinition() {
		ArrayList<Field> bla = new ArrayList<Field>();
		
		bla.add(new Field("RV_MERCADO","NVARCHAR(40),",0));
		bla.add(new Field("RV_NEMO","NVARCHAR(40),",1));
		bla.add(new Field("RV_PRE_PULT_TR","DECIMAL(26,8),",2));
		bla.add(new Field("RV_PRE_ULT_TR","DECIMAL(26,8),",3));
		bla.add(new Field("RV_CATEGORIA","DECIMAL(26,8),",4));
		bla.add(new Field("RV_IND_CARTERA","NVARCHAR(40),",5));
		bla.add(new Field("RV_IND_EXCLUIDA","NVARCHAR(40),",6));
		bla.add(new Field("RV_AUT_VT_CORTO","NVARCHAR(40),",7));
		bla.add(new Field("RV_TENDEN_VT_CORTO","NVARCHAR(40),",8));
		bla.add(new Field("RV_IND_LIQ_DCV","NVARCHAR(40),",9));
		bla.add(new Field("RV_BOLSA","NVARCHAR(40),",10));
		bla.add(new Field("RV_MONEDA","DECIMAL(26,8),",11));
		bla.add(new Field("RV_AUT_OD_BLOQUE","NVARCHAR(40),",12));
		bla.add(new Field("RV_COD_RUBRO","NVARCHAR(40),",13));
		bla.add(new Field("RV_PRE_APE_DI_AC","DECIMAL(26,8),",14));
		bla.add(new Field("RV_PRE_MAY_52SEM","DECIMAL(26,8),",15));
		bla.add(new Field("RV_PRE_MEN_52SEM","DECIMAL(26,8),",16));
		bla.add(new Field("RV_PRE_CIE_7DIAS","DECIMAL(26,8),",17));
		bla.add(new Field("RV_PRE_CIE_30DIAS","DECIMAL(26,8),",18));
		bla.add(new Field("RV_PRE_CIE_52SEM","DECIMAL(26,8),",19));
		bla.add(new Field("RV_RENT_7DIAS","DECIMAL(26,8),",20));
		bla.add(new Field("RV_RENT_30DIAS","DECIMAL(26,8),",21));
		bla.add(new Field("RV_RENT_52SEM","DECIMAL(26,8),",22));
		bla.add(new Field("RV_DIVIDEND_YIELD","DECIMAL(26,8),",23));
		bla.add(new Field("RV_BETA_90DIAS","DECIMAL(26,8),",24));
		bla.add(new Field("RV_CODIGO_ISIN","NVARCHAR(40),",25));
		bla.add(new Field("RV_P_IPSA","DECIMAL(26,8),",26));
		bla.add(new Field("RV_P_IGPA","DECIMAL(26,8),",27));
		bla.add(new Field("RV_P_INTER_10","DECIMAL(26,8),",28));
		bla.add(new Field("RV_ACCIONES_EQ","DECIMAL(26,8),",29));
		bla.add(new Field("RV_VALOR_EQ","DECIMAL(26,8),",30));
		bla.add(new Field("RV_NON_FFLOAT","DECIMAL(26,8),",31));
		bla.add(new Field("RV_SECTOR","NVARCHAR(40),",32));
		bla.add(new Field("RV_PRE_CIE_TEORICO","DECIMAL(26,8),",33));
		bla.add(new Field("RV_BOLSA_EX","NVARCHAR(40),",34));
		bla.add(new Field("RV_MONEDA_EX","NVARCHAR(40),",35));
		bla.add(new Field("RV_PAIS_EMISOR","NVARCHAR(40),",36));
		bla.add(new Field("RV_TIPO_INSTRU","NVARCHAR(40),",37));
		bla.add(new Field("RV_VOL_52SEM","DECIMAL(26,8),",38));
		bla.add(new Field("RV_LISTA","NVARCHAR(40),",39));
		bla.add(new Field("CF_NEMO","NVARCHAR(40),",40));
		bla.add(new Field("CF_LOTE_PADRON","DECIMAL(26,8),",41));
		bla.add(new Field("CF_CESA_PAGO","NVARCHAR(40),",42));
		bla.add(new Field("CF_COD_OFI","NVARCHAR(40),",43));
		bla.add(new Field("CF_COD_OFI_SASA","NVARCHAR(40),",44));
		bla.add(new Field("CF_COND_COD_DI_AC_001","NVARCHAR(40),",45));
		bla.add(new Field("CF_COND_COD_DI_AC_002","NVARCHAR(40),",46));
		bla.add(new Field("CF_COND_COD_DI_AC_003","NVARCHAR(40),",47));
		bla.add(new Field("CF_COND_COD_DI_AC_004","NVARCHAR(40),",48));
		bla.add(new Field("CF_COND_COD_DI_AC_005","NVARCHAR(40),",49));
		bla.add(new Field("CF_COND_COD_DI_AC_006","NVARCHAR(40),",50));
		bla.add(new Field("CF_COND_COD_DI_AC_007","NVARCHAR(40),",51));
		bla.add(new Field("CF_COND_COD_DI_AC_008","NVARCHAR(40),",52));
		bla.add(new Field("CF_COND_COD_DI_AC_009","NVARCHAR(40),",53));
		bla.add(new Field("CF_COND_COD_DI_AC_010","NVARCHAR(40),",54));
		bla.add(new Field("CF_COND_COD_OFI_001","NVARCHAR(40),",55));
		bla.add(new Field("CF_COND_COD_OFI_002","NVARCHAR(40),",56));
		bla.add(new Field("CF_COND_COD_OFI_003","NVARCHAR(40),",57));
		bla.add(new Field("CF_COND_COD_OFI_004","NVARCHAR(40),",58));
		bla.add(new Field("CF_COND_COD_OFI_005","NVARCHAR(40),",59));
		bla.add(new Field("CF_COND_COD_OFI_006","NVARCHAR(40),",60));
		bla.add(new Field("CF_COND_COD_OFI_007","NVARCHAR(40),",61));
		bla.add(new Field("CF_COND_COD_OFI_008","NVARCHAR(40),",62));
		bla.add(new Field("CF_COND_COD_OFI_009","NVARCHAR(40),",63));
		bla.add(new Field("CF_COND_COD_OFI_010","NVARCHAR(40),",64));
		bla.add(new Field("CF_COND_DIS_AN_AN","NVARCHAR(40),",65));
		bla.add(new Field("CF_COND_DIS_DI_AN","NVARCHAR(40),",66));
		bla.add(new Field("CF_COND_DIS_DI_AC","NVARCHAR(40),",67));
		bla.add(new Field("CF_COND_DIS_ME_AN","NVARCHAR(40),",68));
		bla.add(new Field("CF_COND_DIS_OFI","NVARCHAR(40),",69));
		bla.add(new Field("CF_COND_DIS_SE_AN","NVARCHAR(40),",70));
		bla.add(new Field("CF_COND_DIS_SN_AN","NVARCHAR(40),",71));
		bla.add(new Field("CF_CUO_INSCRIP","DECIMAL(26,8),",72));
		bla.add(new Field("CF_DIVIDENDOS","DECIMAL(26,8),",73));
		bla.add(new Field("CF_DIAS_TR_ME_AC","DECIMAL(26,8),",74));
		bla.add(new Field("CF_DIAS_TR_SE_AC","DECIMAL(26,8),",75));
		bla.add(new Field("CF_DIAS_TR_AN_AC","DECIMAL(26,8),",76));
		bla.add(new Field("CF_DIAS_TR_TR_AC","DECIMAL(26,8),",77));
		bla.add(new Field("CF_EMISOR","NVARCHAR(40),",78));
		bla.add(new Field("CF_ESTADO","NVARCHAR(40),",79));
		bla.add(new Field("CF_FEC_EST_SIT","NVARCHAR(40),",80));
		bla.add(new Field("CF_FEC_FIJ_CIE","NVARCHAR(40),",81));
		bla.add(new Field("CF_FEC_INSCRIP","NVARCHAR(40),",82));
		bla.add(new Field("CF_FEC_ULT_TR","NVARCHAR(40),",83));
		bla.add(new Field("CF_HORA_FIJ_CIE","NVARCHAR(40),",84));
		bla.add(new Field("CF_IND_AJU_VC_ULT_TR","NVARCHAR(40),",85));
		bla.add(new Field("CF_IND_SIT_ESP","NVARCHAR(40),",86));
		bla.add(new Field("CF_IND_HISBUR","NVARCHAR(40),",87));
		bla.add(new Field("CF_IND_INSTRU","NVARCHAR(40),",88));
		bla.add(new Field("CF_INVER_ME","DECIMAL(26,8),",89));
		bla.add(new Field("CF_INVER_SE","DECIMAL(26,8),",90));
		bla.add(new Field("CF_INVER_AN","DECIMAL(26,8),",91));
		bla.add(new Field("CF_LIQUIDA","NVARCHAR(40),",92));
		bla.add(new Field("CF_MONTO_AN_AC","DECIMAL(26,8),",93));
		bla.add(new Field("CF_MONTO_AN_AN","DECIMAL(26,8),",94));
		bla.add(new Field("CF_MONTO_DI_AC","DECIMAL(26,8),",95));
		bla.add(new Field("CF_MONTO_DI_AN","DECIMAL(26,8),",96));
		bla.add(new Field("CF_MONTO_ME_AC","DECIMAL(26,8),",97));
		bla.add(new Field("CF_MONTO_ME_AN","DECIMAL(26,8),",98));
		bla.add(new Field("CF_MONTO_PMED_DI_AC","DECIMAL(26,8),",99));
		bla.add(new Field("CF_MONTO_PMED_SN_AC","DECIMAL(26,8),",100));
		bla.add(new Field("CF_MONTO_PMED_ME_AC","DECIMAL(26,8),",101));
		bla.add(new Field("CF_MONTO_PMED_SE_AC","DECIMAL(26,8),",102));
		bla.add(new Field("CF_MONTO_PMED_AN_AC","DECIMAL(26,8),",103));
		bla.add(new Field("CF_MONTO_PROM_UF_3M","DECIMAL(26,8),",104));
		bla.add(new Field("CF_MONTO_SE_AC","DECIMAL(26,8),",105));
		bla.add(new Field("CF_MONTO_SE_AN","DECIMAL(26,8),",106));
		bla.add(new Field("CF_MONTO_SN_AC","DECIMAL(26,8),",107));
		bla.add(new Field("CF_MONTO_SN_AN","DECIMAL(26,8),",108));
		bla.add(new Field("CF_NAA","DECIMAL(26,8),",109));
		bla.add(new Field("CF_NEMO_SASA","NVARCHAR(40),",110));
		bla.add(new Field("CF_RAZON_SOCIAL","NVARCHAR(40),",111));
		bla.add(new Field("CF_NUM_DEC_PRE","DECIMAL(26,8),",112));
		bla.add(new Field("CF_CUO_CIR_ME_AC","DECIMAL(26,8),",113));
		bla.add(new Field("CF_CUO_31DIC","DECIMAL(26,8),",114));
		bla.add(new Field("CF_NUM_MES_UTI","DECIMAL(26,8),",115));
		bla.add(new Field("CF_NUM_NEG_AN_AC","DECIMAL(26,8),",116));
		bla.add(new Field("CF_NUM_NEG_AN_AN","DECIMAL(26,8),",117));
		bla.add(new Field("CF_NUM_NEG_DI_AC","DECIMAL(26,8),",118));
		bla.add(new Field("CF_NUM_NEG_DI_AN","DECIMAL(26,8),",119));
		bla.add(new Field("CF_NUM_NEG_ME_AC","DECIMAL(26,8),",120));
		bla.add(new Field("CF_NUM_NEG_ME_AN","DECIMAL(26,8),",121));
		bla.add(new Field("CF_NUM_NEG_SE_AC","DECIMAL(26,8),",122));
		bla.add(new Field("CF_NUM_NEG_SE_AN","DECIMAL(26,8),",123));
		bla.add(new Field("CF_NUM_NEG_SN_AC","DECIMAL(26,8),",124));
		bla.add(new Field("CF_NUM_NEG_SN_AN","DECIMAL(26,8),",125));
		bla.add(new Field("CF_NUM_REDON","DECIMAL(26,8),",126));
		bla.add(new Field("CF_PATRI_AJU","DECIMAL(26,8),",127));
		bla.add(new Field("CF_PATRI_CON","DECIMAL(26,8),",128));
		bla.add(new Field("CF_PATRI_AN_AC","DECIMAL(26,8),",129));
		bla.add(new Field("CF_PATRI_AN_AN","DECIMAL(26,8),",130));
		bla.add(new Field("CF_PATRI_ME_AC","DECIMAL(26,8),",131));
		bla.add(new Field("CF_PATRI_ME_AN","DECIMAL(26,8),",132));
		bla.add(new Field("CF_PATRI_NEG","NVARCHAR(40),",133));
		bla.add(new Field("CF_PATRI_SE_AC","DECIMAL(26,8),",134));
		bla.add(new Field("CF_PATRI_SE_AN","DECIMAL(26,8),",135));
		bla.add(new Field("CF_PRESEN_AN_AC","DECIMAL(26,8),",136));
		bla.add(new Field("CF_PRESEN_AN_AN","DECIMAL(26,8),",137));
		bla.add(new Field("CF_PRESEN_DIN","DECIMAL(26,8),",138));
		bla.add(new Field("CF_PRESEN_ME_AC","DECIMAL(26,8),",139));
		bla.add(new Field("CF_PRESEN_ME_AN","DECIMAL(26,8),",140));
		bla.add(new Field("CF_PRESEN_SE_AC","DECIMAL(26,8),",141));
		bla.add(new Field("CF_PRESEN_SE_AN","DECIMAL(26,8),",142));
		bla.add(new Field("CF_PRESEN_PRO_12_M","DECIMAL(26,8),",143));
		bla.add(new Field("CF_PRESEN_PRO_1_T","DECIMAL(26,8),",144));
		bla.add(new Field("CF_PRESEN_PRO_2_T","DECIMAL(26,8),",145));
		bla.add(new Field("CF_PRESEN_PRO_3_T","DECIMAL(26,8),",146));
		bla.add(new Field("CF_PRESEN_PRO_4_T","DECIMAL(26,8),",147));
		bla.add(new Field("CF_PRE_CIE_AN_AN","DECIMAL(26,8),",148));
		bla.add(new Field("CF_PRE_CIE_DI_AC","DECIMAL(26,8),",149));
		bla.add(new Field("CF_PRE_CIE_DI_AN","DECIMAL(26,8),",150));
		bla.add(new Field("CF_PRE_CIE_ME_AN","DECIMAL(26,8),",151));
		bla.add(new Field("CF_PRE_CIE_OFI","DECIMAL(26,8),",152));
		bla.add(new Field("CF_PRE_CIE_PEN","DECIMAL(26,8),",153));
		bla.add(new Field("CF_PRE_CIE_SE_AN","DECIMAL(26,8),",154));
		bla.add(new Field("CF_PRE_CIE_SN_AN","DECIMAL(26,8),",155));
		bla.add(new Field("CF_PRE_MAY_AN_AC","DECIMAL(26,8),",156));
		bla.add(new Field("CF_PRE_MAY_AN_AN","DECIMAL(26,8),",157));
		bla.add(new Field("CF_PRE_MAY_DI_AC","DECIMAL(26,8),",158));
		bla.add(new Field("CF_PRE_MAY_DI_AN","DECIMAL(26,8),",159));
		bla.add(new Field("CF_PRE_MAY_ME_AC","DECIMAL(26,8),",160));
		bla.add(new Field("CF_PRE_MAY_ME_AN","DECIMAL(26,8),",161));
		bla.add(new Field("CF_PRE_MAY_SE_AC","DECIMAL(26,8),",162));
		bla.add(new Field("CF_PRE_MAY_SE_AN","DECIMAL(26,8),",163));
		bla.add(new Field("CF_PRE_MAY_SN_AC","DECIMAL(26,8),",164));
		bla.add(new Field("CF_PRE_MAY_SN_AN","DECIMAL(26,8),",165));
		bla.add(new Field("CF_PRE_MED_AN_AC","DECIMAL(26,8),",166));
		bla.add(new Field("CF_PRE_MED_AN_AN","DECIMAL(26,8),",167));
		bla.add(new Field("CF_PRE_MED_DI_AC","DECIMAL(26,8),",168));
		bla.add(new Field("CF_PRE_MED_DI_AN","DECIMAL(26,8),",169));
		bla.add(new Field("CF_PRE_MED_ME_AC","DECIMAL(26,8),",170));
		bla.add(new Field("CF_PRE_MED_ME_AN","DECIMAL(26,8),",171));
		bla.add(new Field("CF_PRE_MED_ULT_TR","DECIMAL(26,8),",172));
		bla.add(new Field("CF_PRE_MED_SE_AC","DECIMAL(26,8),",173));
		bla.add(new Field("CF_PRE_MED_SE_AN","DECIMAL(26,8),",174));
		bla.add(new Field("CF_PRE_MED_SN_AC","DECIMAL(26,8),",175));
		bla.add(new Field("CF_PRE_MED_SN_AN","DECIMAL(26,8),",176));
		bla.add(new Field("CF_PRE_MEN_AN_AC","DECIMAL(26,8),",177));
		bla.add(new Field("CF_PRE_MEN_AN_AN","DECIMAL(26,8),",178));
		bla.add(new Field("CF_PRE_MEN_DI_AC","DECIMAL(26,8),",179));
		bla.add(new Field("CF_PRE_MEN_DI_AN","DECIMAL(26,8),",180));
		bla.add(new Field("CF_PRE_MEN_ME_AC","DECIMAL(26,8),",181));
		bla.add(new Field("CF_PRE_MEN_ME_AN","DECIMAL(26,8),",182));
		bla.add(new Field("CF_PRE_MEN_SE_AC","DECIMAL(26,8),",183));
		bla.add(new Field("CF_PRE_MEN_SE_AN","DECIMAL(26,8),",184));
		bla.add(new Field("CF_PRE_MEN_SN_AC","DECIMAL(26,8),",185));
		bla.add(new Field("CF_PRE_MEN_SN_AN","DECIMAL(26,8),",186));
		bla.add(new Field("CF_REL_BOL_LIB","DECIMAL(26,8),",187));
		bla.add(new Field("CF_REL_PRE_DIV","DECIMAL(26,8),",188));
		bla.add(new Field("CF_REL_PRE_UTI","DECIMAL(26,8),",189));
		bla.add(new Field("CF_RENT_AN_AC","DECIMAL(26,8),",190));
		bla.add(new Field("CF_RENT_AN_AN","DECIMAL(26,8),",191));
		bla.add(new Field("CF_RENT_ME_AC","DECIMAL(26,8),",192));
		bla.add(new Field("CF_RENT_ME_AN","DECIMAL(26,8),",193));
		bla.add(new Field("CF_RENT_SE_AC","DECIMAL(26,8),",194));
		bla.add(new Field("CF_RENT_SE_AN","DECIMAL(26,8),",195));
		bla.add(new Field("CF_TENDEN_DI_AC","NVARCHAR(40),",196));
		bla.add(new Field("CF_TIPO_ENT","NVARCHAR(40),",197));
		bla.add(new Field("CF_PRE_ULT_DI_AC","DECIMAL(26,8),",198));
		bla.add(new Field("CF_UNI_PMED_DI_AC","DECIMAL(26,8),",199));
		bla.add(new Field("CF_UNI_PMED_SN_AC","DECIMAL(26,8),",200));
		bla.add(new Field("CF_UNI_PMED_ME_AC","DECIMAL(26,8),",201));
		bla.add(new Field("CF_UNI_PMED_SE_AC","DECIMAL(26,8),",202));
		bla.add(new Field("CF_UNI_PMED_AN_AC","DECIMAL(26,8),",203));
		bla.add(new Field("CF_UNI_TRA_AN_AC","DECIMAL(26,8),",204));
		bla.add(new Field("CF_UNI_TRA_AN_AN","DECIMAL(26,8),",205));
		bla.add(new Field("CF_UNI_TRA_DI_AC","DECIMAL(26,8),",206));
		bla.add(new Field("CF_UNI_TRA_DI_AN","DECIMAL(26,8),",207));
		bla.add(new Field("CF_UNI_TRA_ME_AC","DECIMAL(26,8),",208));
		bla.add(new Field("CF_UNI_TRA_ME_AN","DECIMAL(26,8),",209));
		bla.add(new Field("CF_UNI_TRA_ULT_TR","DECIMAL(26,8),",210));
		bla.add(new Field("CF_UNI_TRA_SE_AC","DECIMAL(26,8),",211));
		bla.add(new Field("CF_UNI_TRA_SE_AN","DECIMAL(26,8),",212));
		bla.add(new Field("CF_UNI_TRA_SN_AC","DECIMAL(26,8),",213));
		bla.add(new Field("CF_UNI_TRA_SN_AN","DECIMAL(26,8),",214));
		bla.add(new Field("CF_UTI_12_M","DECIMAL(26,8),",215));
		bla.add(new Field("CF_UTI_NUM_MES","DECIMAL(26,8),",216));
		bla.add(new Field("CF_TIPO_FONDO","NVARCHAR(40),",217));
		bla.add(new Field("CF_AUT_OPE_SIMUL","NVARCHAR(40),",218));
		bla.add(new Field("CF_NEMO_DCV","NVARCHAR(40),",219));
		bla.add(new Field("SOC_RUT_ADMIN","NVARCHAR(40),",220));
		bla.add(new Field("SOC_NOM_ADMIN","NVARCHAR(40),",221));

		
		
		return bla;
	}
	
	public ArrayList<Field> getDefinition(String destino) throws Exception {
		if (destino.equals("TBACMI"))
			return getTBACMIDefinition();
		else if (destino.equals("TBCFMI"))
			return getTBCFMIDefinition();
		else if (destino.equals("TBMOMI"))
			return getTBMOMIDefinition();
		else if (destino.equals("TBFUMI"))
			return getTBFUMIDefinition();
		else if (destino.equals("TBRFMI"))
			return getTBRFMIDefinition();
		else if (destino.equals("TBUTEMIS"))
			return getTBUTEMISDefinition();
		else if (destino.equals("TBACFVC"))
			return getTBACFVCDefinition();
		else if (destino.equals("VectorPrecios"))
			return getVectorPreciosDefinition();
		throw new Exception("no se encontro definicion");
	}
	
	public void go(Sql2o sql2ox, boolean isTestMode) throws InterruptedException, Throwable {
	
		log.write("COMENZANDO");
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		CallableStatement call = null;
		
		Long idEmpresa = null;
		
		if (isTestMode) {
			idEmpresa = 1L;
			
		}
		else {
			idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
		}
		
		
		//EXTRACTOR DE ARCHIVOS
			
		String deleteTable = "DELETE FROM ##NOMBRE_TABLA;";
		
		HashMap<String,String> tablas = new HashMap<String,String>();
		tablas.put("TBACMI", "\"BSCL.Plataforma.BD::MER.CargaInstrumentosAcciones\"");
		tablas.put("TBCFMI", "\"BSCL.Plataforma.BD::MER.CargaInstrumentosCuotasFondo\"");
		tablas.put("TBMOMI", "\"BSCL.Plataforma.BD::MER.CargaInstrumentosMonetarios\"");
		tablas.put("TBFUMI", "\"BSCL.Plataforma.BD::MER.CargaInstrumentosFuturos\"");
		tablas.put("TBUTEMIS", "\"BSCL.Plataforma.BD::MER.CargaEmisores\"");
		tablas.put("TBRFMI", "\"BSCL.Plataforma.BD::MER.CargaInstrumentosRentaFija\"");
		tablas.put("VectorPrecios", "\"BSCL.Plataforma.BD::MER.CargaVectorPrecios\"");
		tablas.put("TBACFVC", "\"BSCL.Custodia.BD::CUS.EventoCapitalBursatil\"");
		
		
		
		HashMap<String,String> sps = new HashMap<String,String>();
		sps.put("TBACMI", "CALL \"BSCL.Plataforma.BD.Servicios::MER.ActualizaInstrumentosAcciones\"(p_limpiaTablas => ?,p_out_Resultado => ?, p_out_Mensaje => ?)");
		sps.put("TBCFMI", "CALL \"BSCL.Plataforma.BD.Servicios::MER.ActualizaInstrumentosCuotasFondo\"(p_limpiaTablas => ?,p_out_Resultado => ?, p_out_Mensaje => ?)");
		sps.put("TBMOMI", "CALL \"BSCL.Plataforma.BD.Servicios::MER.ActualizaInstrumentosMonetarios\"(p_limpiaTablas => ?,p_out_Resultado => ?, p_out_Mensaje => ?)");
		sps.put("TBFUMI", "CALL \"BSCL.Plataforma.BD.Servicios::MER.ActualizaInstrumentosFuturos\"(p_limpiaTablas => ?,p_out_Resultado => ?, p_out_Mensaje => ?)");
		sps.put("TBUTEMIS", "CALL \"BSCL.Plataforma.BD.Servicios::MER.ActualizaEmisores\"(p_limpiaTablas => ?,p_out_Resultado => ?, p_out_Mensaje => ?)");
		sps.put("TBRFMI", "CALL \"BSCL.Plataforma.BD.Servicios::MER.ActualizaInstrumentosRentaFija\"(p_limpiaTablas => ?,p_out_Resultado => ?, p_out_Mensaje => ?)");
		sps.put("VectorPrecios", "CALL \"BSCL.Plataforma.BD.Servicios::MER.ActualizaVectorPrecios\"(p_limpiaTablas => ?,p_out_Resultado => ?, p_out_Mensaje => ?)");
		
		//CARGA ARCHIVOS
		List<ExtractorDeArchivo> archivosExtraidos;
		
		try (org.sql2o.Connection conn = sql2ox.beginTransaction()) {
			archivosExtraidos = conn.createQuery(ExtractorDeArchivo.getSelectExtractorDeArchivo(),false)
					.addParameter("IdEmpresa", idEmpresa)
					.executeAndFetch(ExtractorDeArchivo.class);
			
			
			
			for (ExtractorDeArchivo archivoExtraido : archivosExtraidos) {
				
				if (archivoExtraido.Prioridad == 1) {
					log.write("LEYENDO " + archivoExtraido.Archivo + " - " + archivoExtraido.FechaCarga);
					List<String> lineas = conn.createQuery(ExtractorDeArchivo.getSelectDetalleExtractorDeArchivo(),false)
							.addParameter("IdEmpresa", idEmpresa)
							.addParameter("Archivo", archivoExtraido.Archivo)
							.addParameter("FechaCarga", archivoExtraido.FechaCarga)
				    		.executeAndFetch(String.class);
					
					archivoExtraido.lineas = lineas;
				}
			}
		}
		
		//GUARDA EN TABLAS DE PASO
		for (Iterator iterator = archivosExtraidos.iterator(); iterator.hasNext();) {
			ExtractorDeArchivo extractorDeArchivo = (ExtractorDeArchivo) iterator.next();		
			
			if (extractorDeArchivo.Prioridad != 1) {
				try (org.sql2o.Connection conn = sql2ox.beginTransaction()) {
					log.write("ARCHIVO IGNORADO: " + extractorDeArchivo.Archivo + " - " + extractorDeArchivo.FechaCarga);
					conn.createQuery(ExtractorDeArchivo.getUpdateExtractorDeArchivo(),false)
						.addParameter("Procesado", "S")
						.addParameter("IdEstado", "IGN")
						.addParameter("IdEmpresa", idEmpresa)
						.addParameter("Archivo", extractorDeArchivo.Archivo)
						.addParameter("FechaCarga", extractorDeArchivo.FechaCarga)
						.executeUpdate();
					conn.commit();
				}
				continue; //archivo ignorado
			}
						
			try (org.sql2o.Connection conn = sql2ox.beginTransaction()) {
				//borra tabla temporal
				log.write("LIMPIANDO " + tablas.get(extractorDeArchivo.Destino) + " PARA CARGAR DESTINO "  + extractorDeArchivo.Destino);
				org.sql2o.Query deleteTemp = conn.createQuery(deleteTable.replace("##NOMBRE_TABLA",tablas.get(extractorDeArchivo.Destino)), false);
				deleteTemp.executeUpdate();
				conn.commit();
			}
			
			if (tablas.get(extractorDeArchivo.Destino) == null) {
				log.write("DESTINO NO CONFIGURADO " + extractorDeArchivo.Destino);
				continue;
			}
			
			try (org.sql2o.Connection conn = sql2ox.beginTransaction()) {
								
				//insert batch
				ArrayList<Field> fields = getDefinition(extractorDeArchivo.Destino);
				org.sql2o.Query insertTemp = conn.createQuery(getInsertBatchString(tablas.get(extractorDeArchivo.Destino),extractorDeArchivo.Destino), false);

				for (int indexLinea = 0 ;indexLinea<extractorDeArchivo.lineas.size();indexLinea++ ) {
					String[] line = extractorDeArchivo.lineas.get(indexLinea).split(";",-1);
					//log.write(extractorDeArchivo.lineas.get(indexLinea));
					for (int j = 0; j < fields.size(); j++) {
						insertTemp.addParameter(fields.get(j).fieldName, line[fields.get(j).index].trim());
					}
					insertTemp.addToBatch();
					/*if (indexLinea % 1 == 0) {
						log.write("INDEX " + indexLinea);
						i*nsertTemp.executeBatch();
					}*/
				}
				log.write("INSERTANDO DATOS EN " + tablas.get(extractorDeArchivo.Destino));
				insertTemp.executeBatch();
				conn.commit();

			}
				
			try (org.sql2o.Connection conn = sql2ox.beginTransaction()) {
				
				String storedProc = sps.get(extractorDeArchivo.Destino);
				if (storedProc == null) {
					log.write("PROCEDIMIENTO NO CONFIGURADO PARA DESTINO " + extractorDeArchivo.Destino);
					conn.commit();
					conn.close();
					continue;
					//no podemos procesar, no sabemos con que SP
				}
				
				call = conn.getJdbcConnection().prepareCall(storedProc);
				call.setString(1, "N");	//LIMPIA TABLAS
				
				/* SI ALGUN DESTINO TIENE PARAMETROS DE ENTRADA DIFERENTE, COLOCAR AQUI EN IF */
				
				call.registerOutParameter(2, Types.NVARCHAR);
				call.registerOutParameter(3, Types.NVARCHAR);
				boolean hasResults = call.execute();
				log.write("RESULTADO: " + call.getString(2) + " - MENSAJE: " + call.getString(3) );
				
				conn.createQuery(ExtractorDeArchivo.getUpdateExtractorDeArchivo(),false)
					.addParameter("Procesado", "S")
					.addParameter("IdEstado", "PRO")
					.addParameter("IdEmpresa", idEmpresa)
					.addParameter("Archivo", extractorDeArchivo.Archivo)
					.addParameter("FechaCarga", extractorDeArchivo.FechaCarga)
					.executeUpdate();
				conn.commit();
			}
			catch(Exception e) {
				try (org.sql2o.Connection connErr = sql2ox.beginTransaction()) {
					connErr.createQuery(ExtractorDeArchivo.getUpdateExtractorDeArchivo(),false)
					.addParameter("Procesado", "S")
					.addParameter("IdEstado", "ERR")
					.addParameter("IdEmpresa", idEmpresa)
					.addParameter("Archivo", extractorDeArchivo.Archivo)
					.addParameter("FechaCarga", extractorDeArchivo.FechaCarga)
					.executeUpdate();
					connErr.commit();
				}
				log.write("Archivo con error " + extractorDeArchivo.Archivo);
				log.write(e.getMessage());
				
			}
			
			
		}
		log.write("FIN JOB");
	}
	
	class Field {
		String fieldName;
		String fieldType;
		int index; //indice al hacer split de la fila
		
		public Field(String fn,String ft,int index) {
			this.fieldName = fn;
			this.fieldType = ft;
			//this.javaType = jt;
			this.index = index;
		}
	}
	
	public String getInsertBatchString(String tableName, String destino) throws Exception {
		ArrayList<Field> campos = getDefinition(destino);
		String sql1 = "INSERT INTO " +tableName + "(";
		String sql2 = "VALUES (";
		for (int i = 0; i < campos.size(); i++) {
			sql1+="\""+campos.get(i).fieldName+"\"";
			sql2+=":"+campos.get(i).fieldName;
			if (i < campos.size() - 1) {
				sql1 += ",";
				sql2 += ",";
			}
		}
		sql1 +=")\n";
		sql2 +=")";
		return sql1 + sql2;
	}
	
	@Override
    public void onError(Throwable t) {
        log.write(":Procesador de Archivos:  ****** ERROR  *******",t);  
    }

	@Override
	public void pause() {

	}
	
	private static final String DB_DRIVER = "com.sap.db.jdbc.Driver";
	
	private static final String DB_CONNECTION_QA = "jdbc:sap://161.238.34.246:30315/?autocommit=false&currentschema=BSCL";
	private static final String DB_USER_QA = "MDCLIENT";
	private static final String DB_PASSWORD_QA = "B0lsa1234";
	
	private static final String DB_CONNECTION_DESA = "jdbc:sap://161.238.34.246:30215/?autocommit=false&currentschema=BSCL";
	private static final String DB_USER_DESA = "MDCLIENT";
	private static final String DB_PASSWORD_DESA = "Bolsa123";
	
	private static final String DB_CONNECTION_UAT = "jdbc:sap://161.238.37.7:30015/?autocommit=false&currentschema=BSCL";
	private static final String DB_USER_UAT = "MDCLIENT";
	private static final String DB_PASSWORD_UAT = "B0lsa123";
	
	private static final String DB_CONNECTION_UAT_2 = "jdbc:sap://161.238.37.7:30115/?autocommit=false&currentschema=BSCL";
	private static final String DB_USER_UAT_2 = "SYSTEM";
	private static final String DB_PASSWORD_UAT_2 = "B0lsa1234";
	
	public static void main(String[] args){
	
		ProcesadorArchivosJob p = new ProcesadorArchivosJob();
		Sql2o sql2ox = new Sql2o(DB_CONNECTION_DESA, DB_USER_DESA, DB_PASSWORD_DESA);
		try {
			p.go(sql2ox,true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}

class ExtractorDeArchivo {
	String Destino;
	String Archivo;
	String FechaCarga;
	int Prioridad;
	List<String> lineas;
	
	public static String getSelectExtractorDeArchivo() {
		return "SELECT RANK() OVER (PARTITION BY \"IdEmpresa\",\"Destino\" order by \"FechaCarga\" desc) as \"Prioridad\",\"Archivo\",\"FechaCarga\",\"Destino\" FROM \"BSCL.Plataforma.BD::PLA.ExtractorDeArchivo\" WHERE \"Procesado\" = 'N' AND \"IdEmpresa\"= :IdEmpresa order by \"FechaCarga\" asc";
	}
	
	public static String getSelectDetalleExtractorDeArchivo() {
		return "SELECT \"Linea\" FROM \"BSCL.Plataforma.BD::PLA.DetalleExtractorDeArchivo\" WHERE \"IdEmpresa\"= :IdEmpresa AND \"Archivo\" = :Archivo AND \"FechaCarga\" = :FechaCarga order by \"NumeroDeLinea\"";
	}

	public static String getUpdateExtractorDeArchivo() {
		return "UPDATE \"BSCL.Plataforma.BD::PLA.ExtractorDeArchivo\" SET \"Procesado\" = :Procesado, \"IdEstado\" = :IdEstado WHERE \"IdEmpresa\" = :IdEmpresa AND \"Archivo\" = :Archivo AND \"FechaCarga\" = :FechaCarga";
	}

	
}
	