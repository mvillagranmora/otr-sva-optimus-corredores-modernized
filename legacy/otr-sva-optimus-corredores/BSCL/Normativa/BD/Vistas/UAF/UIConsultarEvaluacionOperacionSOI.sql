
SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Normativa.BD.Vistas::UAF.UIConsultarEvaluacionOperacionSOI"; 
GO
CREATE VIEW "BSCL.Normativa.BD.Vistas::UAF.UIConsultarEvaluacionOperacionSOI" AS

	SELECT
		   SOI."IdEvaluacion",
		   SOI."IdEmpresa",
		   SOI."IdModulo",
		   SOI."IdCliente",
		   CLI."NombreCompleto" 			AS "NombreCliente",
		   CLI."DocumentoIdentidad",
		   COALESCE(PORT."CodigoPortafolio",'') AS "CodigoPortafolio", 	
		   COALESCE(PORT."IdPerfilProductos",'0') AS "IdPerfilProductos",
		   SOI."IdPortafolio",
		   SOI."IdTipoProducto",
		   COALESCE(PRO."Nombre",'') 		AS "TipoProducto",
		   SOI."IdTipoOperacion",
		   COALESCE(OPER."Nombre",COALESCE(TipoMovimiento."Nombre",''))		AS "TipoOperacion",
		   SOI."IdOperacion",
		   SOI."MontoUtilizado",
		   SOI."IdMoneda",
		   COALESCE(Moneda."Nombre",'')		AS "Moneda",
		   SOI."IdTipoInstrumento",
		   COALESCE(INS."Nombre",'')		AS "TipoInstrumento",
		   SOI."IdClasificacionTipoInstrumento",
   		   COALESCE(CLAS."Nombre",'')		AS "ClasificacionTipoInstrumento",
		   SOI."FechaOperacion",--'YYYY-MM-DD'
		    ---Evaluacion
		   SOI."TipoEvaluacion",
		   SOI."EstMontoTotalDias",
    	   SOI."EstCantidadDias",
		   SOI."EstMontoPromedioDiario",
		   SOI."EstMontoMaximo",
		   SOI."Patrimonio",
		   (CASE SOI."TipoEvaluacion"
		   			 WHEN 'MONTO_MAXIMO' 	THEN SOI."EstMontoMaximo"
					 WHEN 'MONTO_PROMEDIO' 	THEN SOI."EstMontoPromedioDiario"
					 WHEN 'PATRIMONIO'		THEN SOI."Patrimonio"
			ELSE 0 END) AS "MontoEvaluado",		 
			---Calculos   
		   SOI."ConceptoEvaluacion",
		   SOI."CalDiferencia",
		   SOI."CalTolerancia",
		   SOI."Factor",  
			---Auditoria
		   SOI."FechaIngreso",
		   SOI."FechaCreacion",
		   SOI."IdUsuarioCreador",
		   COALESCE(USU."CodigoUsuario",'') AS  "CodigoUsuarioCreador",
		   (CASE WHEN SOI."TipoEvaluacion" ='PERFIL_PRODUCTO' THEN 'PERFIL' ELSE 'MONTO' END) AS "UI_Panel"
		FROM "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI" SOI
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
		ON (CLI."IdCliente"=SOI."IdCliente")
		LEFT OUTER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS PORT
		ON  SOI."IdPortafolio" = PORT."IdPortafolio"
		LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS OPER 
     	ON OPER."IdTipoOperacion" = SOI."IdTipoOperacion"
		LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS Moneda
		ON (Moneda."IdMoneda" = SOI."IdMoneda")
		LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoProducto" PRO
		ON (PRO."IdTipoProducto"=SOI."IdTipoProducto")	
		LEFT OUTER JOIN  "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" USU
		ON (USU."IdUsuario"=SOI."IdUsuarioCreador")
		LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS INS 
     	ON INS."IdTipoInstrumento" = SOI."IdTipoInstrumento"
     	LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CLAS 
     	ON CLAS."IdClasificacionTipoInstrumento" = SOI."IdClasificacionTipoInstrumento"
     	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" AS TipoMovimiento 
     	ON  SOI."IdTipoOperacion" = TipoMovimiento."IdTipoMovimiento" ;			
GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::UAF.UIConsultarEvaluacionOperacionSOI" TO  BCS_DESARROLLADOR WITH GRANT OPTION;