SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarParametrosSistema";
GO

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarParametrosSistema" AS
	SELECT 'TESORERIA' AS "Modulo","IdEmpresa","CodigoParametro","Valor","Descripcion","Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones"	
	FROM "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"
	UNION ALL
	SELECT 'FFMM' AS "Modulo","IdEmpresa","NombreTipoParametro" AS "CodigoParametro","ValorParametro","Descripcion","Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones"
	FROM "BSCL.FondoMutuo.BD::FMM.Parametro"
	UNION ALL
	SELECT 'MESA' AS "Modulo","IdEmpresa","CodigoParametro","Valor" ,"Descripcion","Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones"
	FROM "BSCL.Mesa.BD::MES.ConfiguracionMesa"
	UNION ALL
	SELECT 'ORDENES' AS "Modulo","IdEmpresa","CodigoParametro","Valor" ,"Descripcion","Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones"
	FROM "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
	UNION ALL
	SELECT 'CTAINV' AS "Modulo","IdEmpresa","CodigoParametro","Valor" ,"Descripcion","Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones"
	FROM "BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion"
	UNION ALL
	SELECT 'NORMATIVA' AS "Modulo","IdEmpresa","CodigoParametro","Valor" ,"Descripcion","Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones"
	FROM "BSCL.Normativa.BD::NOR.Configuracion"
	UNION ALL
	SELECT 'PLATAFORMA' AS "Modulo","IdEmpresa","CodigoParametro","Valor","Descripcion" ,"Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones"
	FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	UNION ALL
	SELECT 'FACTURACION' AS "Modulo","IdEmpresa","CodigoParametro","Valor","Descripcion" ,"Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones"
	FROM "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"		
	UNION ALL
	SELECT 'CUSTODIA' AS "Modulo","IdEmpresa","CodigoParametro","Valor","Descripcion" ,"Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones"
	FROM "BSCL.Custodia.BD::CUS.Configuracion"
	UNION ALL
	SELECT 'SOCIONEGOCIO' AS "Modulo", "IdEmpresa", "CodigoParametro", "Valor", "Descripcion", "Uso", "ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones" 
	FROM "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio"
	UNION ALL
	SELECT 'PRODUCTO' AS "Modulo","IdEmpresa","CodigoParametro","Valor","Descripcion","Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones"	
	FROM "BSCL.Plataforma.BD::PRO.Configuracion"
	
	UNION ALL
	SELECT 'CONTABILIDAD' AS "Modulo","IdEmpresa","CodigoParametro","Valor" ,"Descripcion","Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones"	
	FROM "BSCL.Contabilidad.BD::CON.ConfiguracionContabilidad"
	UNION ALL 
	SELECT 'LIMITES' AS "Modulo", "IdEmpresa","Codigo", "Valor","Descripcion","Uso","ValorDefecto","ValoresPosibles","ModificablePorUsuario","ModificablePostProduccion","CreadorParametro","FechaCreacion","ModificadorParametro","FechaModificacion","Observaciones"	
	FROM "BSCL.Plataforma.BD::LIM.Configuracion"
	;
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarParametrosSistema" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
