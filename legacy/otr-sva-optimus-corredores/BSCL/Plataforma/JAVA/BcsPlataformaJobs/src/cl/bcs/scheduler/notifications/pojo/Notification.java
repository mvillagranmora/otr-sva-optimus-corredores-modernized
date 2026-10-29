package cl.bcs.scheduler.notifications.pojo;

public class Notification {
	
	private int  IdEmpresa;       
	private int  IdNotificacion;              
	private int   IdConfiguracionEvento;       
	private String   FechaCreacion;                         
	private String   FormaDespacho;                       
	private String   TiempoDespacho;                       
	private String   FechaDespachoOrginal;                   
	private String   FechaDespachoRecordatorio;              
	private String   Enviado;                               
	private String   FechaEnviado ;                        
	private int   IdUsuarioEnviado;              
	private String   ReEnviar;                              
	private String   ReEnviado;                             
	private String   FechaReEnviado;                         
	private int   IdUsuarioReEnviado;           
	private String   ConfirmacionDespacho;                
	private String   TokenConfirmacion;                   
	private String   Confirmado;                            
	private String   FechaConfirmado;                        
	private String   AccionConfirmada;                     
	private int   IdUsuarioConfimado;            
	private String   FechaVencimiento;                             
	private String   FechaRecordatorio;                             
	private String   Observacion;                         
	private String   Descartado; 				           
	private int   IdUsuarioDescarte;			 				 
	private String   FechaDescarte;
	private Long SecuenciaOrigen;
	
	public int getIdEmpresa() {
		return IdEmpresa;
	}
	public void setIdEmpresa(int idEmpresa) {
		IdEmpresa = idEmpresa;
	}
	public int getIdNotificacion() {
		return IdNotificacion;
	}
	public void setIdNotificacion(int idNotificacion) {
		IdNotificacion = idNotificacion;
	}
	public int getIdConfiguracionEvento() {
		return IdConfiguracionEvento;
	}
	public void setIdConfiguracionEvento(int idConfiguracionEvento) {
		IdConfiguracionEvento = idConfiguracionEvento;
	}
	public String getFechaCreacion() {
		return FechaCreacion;
	}
	public void setFechaCreacion(String fechaCreacion) {
		FechaCreacion = fechaCreacion;
	}
	public String getFormaDespacho() {
		return FormaDespacho;
	}
	public void setFormaDespacho(String formaDespacho) {
		FormaDespacho = formaDespacho;
	}
	public String getTiempoDespacho() {
		return TiempoDespacho;
	}
	public void setTiempoDespacho(String tiempoDespacho) {
		TiempoDespacho = tiempoDespacho;
	}
	public String getFechaDespachoOrginal() {
		return FechaDespachoOrginal;
	}
	public void setFechaDespachoOrginal(String fechaDespachoOrginal) {
		FechaDespachoOrginal = fechaDespachoOrginal;
	}
	public String getFechaDespachoRecordatorio() {
		return FechaDespachoRecordatorio;
	}
	public void setFechaDespachoRecordatorio(String fechaDespachoRecordatorio) {
		FechaDespachoRecordatorio = fechaDespachoRecordatorio;
	}
	public String getEnviado() {
		return Enviado;
	}
	public void setEnviado(String enviado) {
		Enviado = enviado;
	}
	public String getFechaEnviado() {
		return FechaEnviado;
	}
	public void setFechaEnviado(String fechaEnviado) {
		FechaEnviado = fechaEnviado;
	}
	public int getIdUsuarioEnviado() {
		return IdUsuarioEnviado;
	}
	public void setIdUsuarioEnviado(int idUsuarioEnviado) {
		IdUsuarioEnviado = idUsuarioEnviado;
	}
	public String getReEnviar() {
		return ReEnviar;
	}
	public void setReEnviar(String reEnviar) {
		ReEnviar = reEnviar;
	}
	public String getReEnviado() {
		return ReEnviado;
	}
	public void setReEnviado(String reEnviado) {
		ReEnviado = reEnviado;
	}
	public String getFechaReEnviado() {
		return FechaReEnviado;
	}
	public void setFechaReEnviado(String fechaReEnviado) {
		FechaReEnviado = fechaReEnviado;
	}
	public int getIdUsuarioReEnviado() {
		return IdUsuarioReEnviado;
	}
	public void setIdUsuarioReEnviado(int idUsuarioReEnviado) {
		IdUsuarioReEnviado = idUsuarioReEnviado;
	}
	public String getConfirmacionDespacho() {
		return ConfirmacionDespacho;
	}
	public void setConfirmacionDespacho(String confirmacionDespacho) {
		ConfirmacionDespacho = confirmacionDespacho;
	}
	public String getTokenConfirmacion() {
		return TokenConfirmacion;
	}
	public void setTokenConfirmacion(String tokenConfirmacion) {
		TokenConfirmacion = tokenConfirmacion;
	}
	public String getConfirmado() {
		return Confirmado;
	}
	public void setConfirmado(String confirmado) {
		Confirmado = confirmado;
	}
	public String getFechaConfirmado() {
		return FechaConfirmado;
	}
	public void setFechaConfirmado(String fechaConfirmado) {
		FechaConfirmado = fechaConfirmado;
	}
	public String getAccionConfirmada() {
		return AccionConfirmada;
	}
	public void setAccionConfirmada(String accionConfirmada) {
		AccionConfirmada = accionConfirmada;
	}
	public int getIdUsuarioConfimado() {
		return IdUsuarioConfimado;
	}
	public void setIdUsuarioConfimado(int idUsuarioConfimado) {
		IdUsuarioConfimado = idUsuarioConfimado;
	}
	public String getFechaVencimiento() {
		return FechaVencimiento;
	}
	public void setFechaVencimiento(String fechaVencimiento) {
		FechaVencimiento = fechaVencimiento;
	}
	public String getFechaRecordatorio() {
		return FechaRecordatorio;
	}
	public void setFechaRecordatorio(String fechaRecordatorio) {
		FechaRecordatorio = fechaRecordatorio;
	}
	public String getObservacion() {
		return Observacion;
	}
	public void setObservacion(String observacion) {
		Observacion = observacion;
	}
	public String getDescartado() {
		return Descartado;
	}
	public void setDescartado(String descartado) {
		Descartado = descartado;
	}
	public int getIdUsuarioDescarte() {
		return IdUsuarioDescarte;
	}
	public void setIdUsuarioDescarte(int idUsuarioDescarte) {
		IdUsuarioDescarte = idUsuarioDescarte;
	}
	public String getFechaDescarte() {
		return FechaDescarte;
	}
	public void setFechaDescarte(String fechaDescarte) {
		FechaDescarte = fechaDescarte;
	}
	public Long getSecuenciaOrigen() {
		return SecuenciaOrigen; 
	}
	public void setSecuenciaOrigen(Long secuenciaOrigen) {
		SecuenciaOrigen = secuenciaOrigen; 
	}
	public Notification()
	{
		super();
	}
	public Notification(int idEmpresa, int idNotificacion, int idConfiguracionEvento, String fechaCreacion,
			String formaDespacho, String tiempoDespacho, String fechaDespachoOrginal, String fechaDespachoRecordatorio,
			String enviado, String fechaEnviado, int idUsuarioEnviado, String reEnviar, String reEnviado,
			String fechaReEnviado, int idUsuarioReEnviado, String confirmacionDespacho, String tokenConfirmacion,
			String confirmado, String fechaConfirmado, String accionConfirmada, int idUsuarioConfimado,
			String fechaVencimiento, String fechaRecordatorio, String observacion, String descartado,
			int idUsuarioDescarte, String fechaDescarte, Long secuenciaOrigen) {
		super();
		IdEmpresa = idEmpresa;
		IdNotificacion = idNotificacion;
		IdConfiguracionEvento = idConfiguracionEvento;
		FechaCreacion = fechaCreacion;
		FormaDespacho = formaDespacho;
		TiempoDespacho = tiempoDespacho;
		FechaDespachoOrginal = fechaDespachoOrginal;
		FechaDespachoRecordatorio = fechaDespachoRecordatorio;
		Enviado = enviado;
		FechaEnviado = fechaEnviado;
		IdUsuarioEnviado = idUsuarioEnviado;
		ReEnviar = reEnviar;
		ReEnviado = reEnviado;
		FechaReEnviado = fechaReEnviado;
		IdUsuarioReEnviado = idUsuarioReEnviado;
		ConfirmacionDespacho = confirmacionDespacho;
		TokenConfirmacion = tokenConfirmacion;
		Confirmado = confirmado;
		FechaConfirmado = fechaConfirmado;
		AccionConfirmada = accionConfirmada;
		IdUsuarioConfimado = idUsuarioConfimado;
		FechaVencimiento = fechaVencimiento;
		FechaRecordatorio = fechaRecordatorio;
		Observacion = observacion;
		Descartado = descartado;
		IdUsuarioDescarte = idUsuarioDescarte;
		FechaDescarte = fechaDescarte;
		SecuenciaOrigen = secuenciaOrigen;
	}

}
