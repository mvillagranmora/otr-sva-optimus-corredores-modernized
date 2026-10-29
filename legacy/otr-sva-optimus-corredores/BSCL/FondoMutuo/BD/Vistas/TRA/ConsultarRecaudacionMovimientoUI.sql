SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarRecaudacionMovimientoUI";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarRecaudacionMovimientoUI"
AS
 SELECT
	 RECMOV."IdMovimientoRecaudacion"
	,RECMOV."IdRecaudacion"
	,RECMOV."IdEmpresa"	
	,RECMOV."IdUsuario"
	,RECMOV."IdModulo"
	,RECMOV."IdSecuenciaMovimiento"
	,RECMOV."FechaIngreso"
    ,RECMOV."FechaIngresoRegistro"
    ,CTAMOV."IdEstado"
    ,CTAMOV."IdTipoMovimiento" AS "IdTipoMovimiento"
    ,CTAMOV."Monto" AS "Monto"
	,CTAMOV."FechaEfectivaMovimiento" AS "FechaEfectiva"
   -- ,CTA."IdCliente"
   -- ,CTA."IdPortafolio" 
    ,CTA."IdMoneda"  AS "IdMoneda"
   -- ,PORTAFOLIO."CodigoPortafolio"
	,CLIENTE."NombreCompleto"
	,CLIENTE."DocumentoIdentidad"
	,CLIENTE."IdPersona"
	,MODULO."Nombre"  						AS "Modulo"
	,COALESCE(TIPOCTA."Nombre",'') 			AS "TipoMovimiento"
	,COALESCE(ESTADOINV."Nombre",'')		AS "EstadoMovimiento"
 FROM "BSCL.FondoMutuo.BD::TRA.RecaudacionMovimiento" 	AS RECMOV
 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" 	AS 	CTAMOV  		ON 	(RECMOV."IdSecuenciaMovimiento"=CTAMOV."IdCargoAbonoMovimiento" AND RECMOV."IdModulo"='CTA')
 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" 			AS	CTA 			ON 	(CTA."IdCuentaInversion"=CTAMOV."IdCuentaInversion")
 --INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" 					AS  PORTAFOLIO 		ON  (PORTAFOLIO."IdPortafolio" = CTA."IdPortafolio" )
 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS 	CLIENTE 		ON 	(CLIENTE."IdCliente" = CTA."IdCliente")
 INNER JOIN "BSCL.Plataforma.BD::PLA.Modulo"						AS 	MODULO 			ON 	(MODULO."IdModulo"=RECMOV."IdModulo") 
 LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" 			AS  TIPOCTA			ON  (TIPOCTA."IdTipoMovimiento"	=  CTAMOV."IdTipoMovimiento" AND TIPOCTA."IdModulo"='CTA')
 LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" 					AS  ESTADOINV 		ON  (ESTADOINV."IdEstado" 		=  CTAMOV."IdEstado")
UNION ALL
SELECT
	 RECMOV."IdMovimientoRecaudacion"
	,RECMOV."IdRecaudacion"
	,RECMOV."IdEmpresa"	
	,RECMOV."IdUsuario"
	,RECMOV."IdModulo"
	,RECMOV."IdSecuenciaMovimiento"
	,RECMOV."FechaIngreso"
    ,RECMOV."FechaIngresoRegistro"
    ,TESMOV."IdEstado"
    ,TESMOV."IdTipoMovimiento" AS "IdTipoMovimiento"
    ,TESMOV."Monto" AS "Monto"
	,TESMOV."FechaLiquidacion" AS "FechaEfectiva"
    --,CTA."IdCliente"
    --,CTA."IdPortafolio" 
    ,TESMOV."IdMonedaOperacion"  AS "IdMoneda"
   -- ,COALESCE(PORTAFOLIO."CodigoPortafolio",'') AS "CodigoPortafolio"
	,PERSONA."NombreCompleto"
	,PERSONA."DocumentoIdentidad"
	,PERSONA."IdPersona"
	,MODULO."Nombre"  						AS "Modulo"
	,COALESCE(TIPOCTA."Nombre",'') 			AS "TipoMovimiento"
	,COALESCE(ESTADOINV."Nombre",'')		AS "EstadoMovimiento"
 FROM "BSCL.FondoMutuo.BD::TRA.RecaudacionMovimiento" 	AS RECMOV
 INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso"	 	AS TESMOV  		ON 	(RECMOV."IdSecuenciaMovimiento"=TESMOV."IdIngresoEgreso" AND RECMOV."IdModulo"='TES')
 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" 	AS 	PERSONA 		ON 	(PERSONA."IdPersona" = TESMOV."IdPersonaTitularPago")
 INNER JOIN "BSCL.Plataforma.BD::PLA.Modulo"						AS 	MODULO 			ON 	(MODULO."IdModulo"=RECMOV."IdModulo")
 --LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" 					AS  PORTAFOLIO 		ON  (PORTAFOLIO."IdPortafolio" = TESMOV."IdPortafolioTitularPago" )
 LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" 			AS  TIPOCTA			ON  (TIPOCTA."IdTipoMovimiento"	=  TESMOV."IdTipoMovimiento" AND TIPOCTA."IdModulo"='TES')
 LEFT OUTER  JOIN "BSCL.Plataforma.BD::PLA.Estado" 					AS  ESTADOINV 		ON  (ESTADOINV."IdEstado" 		=  TESMOV."IdEstado");


GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarRecaudacionMovimientoUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO