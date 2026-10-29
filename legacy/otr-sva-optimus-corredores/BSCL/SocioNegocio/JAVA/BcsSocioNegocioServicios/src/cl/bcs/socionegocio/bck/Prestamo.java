package cl.bcs.socionegocio.bck;


import java.util.ArrayList;

/**
 * @author Sebastian Hechenleitner
 */

public class Prestamo {

	private String ID_REG;
	private String FECHA_ING;
	private String NEMO;
	private String MERCADO;
	private String RUT_PTTRO;
	private String RUT_PTMTA;
	private String TIP_PRESTAMO;
	private String CANTIDAD;
	private String TASA;
	private String COND_LIQ;
	private String FECHA_LIQ;
	private String TIP_GARANTIA;
	private String COD_CORR_PTTRO;
	private String COD_CORR_PTMTA;
	private String FOLIO_BC;
	private String FOLIO_SB;
	private String HORA_REG;
	private String ESTADO;
	private String MENSAJE;
	private ArrayList<Prestamista> PRESTAMISTA;

	

	public Prestamo(String iD_REG, String fECHA_ING, String nEMO,
			String mERCADO, String rUT_PTTRO, String rUT_PTMTA,
			String tIP_PRESTAMO, String cANTIDAD, String tASA, String cOND_LIQ,
			String fECHA_LIQ, String tIP_GARANTIA, String cOD_CORR_PTTRO,
			String cOD_CORR_PTMTA, String fOLIO_BC, String fOLIO_SB,
			String hORA_REG, String eSTADO, String mENSAJE,
			ArrayList<Prestamista> pRESTAMISTA) {
		super();
		ID_REG = iD_REG;
		FECHA_ING = fECHA_ING;
		NEMO = nEMO;
		MERCADO = mERCADO;
		RUT_PTTRO = rUT_PTTRO;
		RUT_PTMTA = rUT_PTMTA;
		TIP_PRESTAMO = tIP_PRESTAMO;
		CANTIDAD = cANTIDAD;
		TASA = tASA;
		COND_LIQ = cOND_LIQ;
		FECHA_LIQ = fECHA_LIQ;
		TIP_GARANTIA = tIP_GARANTIA;
		COD_CORR_PTTRO = cOD_CORR_PTTRO;
		COD_CORR_PTMTA = cOD_CORR_PTMTA;
		FOLIO_BC = fOLIO_BC;
		FOLIO_SB = fOLIO_SB;
		HORA_REG = hORA_REG;
		ESTADO = eSTADO;
		MENSAJE = mENSAJE;
		PRESTAMISTA = pRESTAMISTA;
	}
	
	public Prestamo(String folio, String mensaje, String estado) {
		FOLIO_BC = folio;
		MENSAJE = mensaje;
		ESTADO = estado;
	}

	public Prestamo(String mensaje) {
		MENSAJE = mensaje;
	}

	public String getID_REG() {
		return ID_REG;
	}

	public void setID_REG(String iD_REG) {
		ID_REG = iD_REG;
	}

	public String getFECHA_ING() {
		return FECHA_ING;
	}

	public void setFECHA_ING(String fECHA_ING) {
		FECHA_ING = fECHA_ING;
	}

	public String getNEMO() {
		return NEMO;
	}

	public void setNEMO(String nEMO) {
		NEMO = nEMO;
	}

	public String getMERCADO() {
		return MERCADO;
	}

	public void setMERCADO(String mERCADO) {
		MERCADO = mERCADO;
	}

	public String getRUT_PTTRO() {
		return RUT_PTTRO;
	}

	public void setRUT_PTTRO(String rUT_PTTRO) {
		RUT_PTTRO = rUT_PTTRO;
	}

	public String getRUT_PTMTA() {
		return RUT_PTMTA;
	}

	public void setRUT_PTMTA(String rUT_PTMTA) {
		RUT_PTMTA = rUT_PTMTA;
	}

	public String getTIP_PRESTAMO() {
		return TIP_PRESTAMO;
	}

	public void setTIP_PRESTAMO(String tIP_PRESTAMO) {
		TIP_PRESTAMO = tIP_PRESTAMO;
	}

	public String getCANTIDAD() {
		return CANTIDAD;
	}

	public void setCANTIDAD(String cANTIDAD) {
		CANTIDAD = cANTIDAD;
	}

	public String getTASA() {
		return TASA;
	}

	public void setTASA(String tASA) {
		TASA = tASA;
	}

	public String getCOND_LIQ() {
		return COND_LIQ;
	}

	public void setCOND_LIQ(String cOND_LIQ) {
		COND_LIQ = cOND_LIQ;
	}

	public String getFECHA_LIQ() {
		return FECHA_LIQ;
	}

	public void setFECHA_LIQ(String fECHA_LIQ) {
		FECHA_LIQ = fECHA_LIQ;
	}

	public String getTIP_GARANTIA() {
		return TIP_GARANTIA;
	}

	public void setTIP_GARANTIA(String tIP_GARANTIA) {
		TIP_GARANTIA = tIP_GARANTIA;
	}

	public String getCOD_CORR_PTTRO() {
		return COD_CORR_PTTRO;
	}

	public void setCOD_CORR_PTTRO(String cOD_CORR_PTTRO) {
		COD_CORR_PTTRO = cOD_CORR_PTTRO;
	}

	public String getCOD_CORR_PTMTA() {
		return COD_CORR_PTMTA;
	}

	public void setCOD_CORR_PTMTA(String cOD_CORR_PTMTA) {
		COD_CORR_PTMTA = cOD_CORR_PTMTA;
	}

	public String getFOLIO_BC() {
		return FOLIO_BC;
	}

	public void setFOLIO_BC(String fOLIO_BC) {
		FOLIO_BC = fOLIO_BC;
	}

	public String getFOLIO_SB() {
		return FOLIO_SB;
	}

	public void setFOLIO_SB(String fOLIO_SB) {
		FOLIO_SB = fOLIO_SB;
	}

	public String getHORA_REG() {
		return HORA_REG;
	}

	public void setHORA_REG(String hORA_REG) {
		HORA_REG = hORA_REG;
	}

	public String getESTADO() {
		return ESTADO;
	}

	public void setESTADO(String eSTADO) {
		ESTADO = eSTADO;
	}

	public String getMENSAJE() {
		return MENSAJE;
	}

	public void setMENSAJE(String mENSAJE) {
		MENSAJE = mENSAJE;
	}

	public ArrayList<Prestamista> getPRESTAMISTA() {
		return PRESTAMISTA;
	}

	public void setPRESTAMISTA(ArrayList<Prestamista> pRESTAMISTA) {
		PRESTAMISTA = pRESTAMISTA;
	}
	
}