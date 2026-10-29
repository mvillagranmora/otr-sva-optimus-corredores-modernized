SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento";

CREATE PROCEDURE
-- 
--  Proposito: Obtiene el libro de un cliente
--  Autor: - BCS
--  Fecha Creacion: 27/07/2015 
--     
"BSCL.Orden.BD.Utilitarios::ObtenerSalidaProcedimiento" ( 
	IN p_SalidaDetalle	"BSCL.Orden.BD.Tipos::SalidaDetalle",
	
	OUT p_out_InformacionContador			INT,
	OUT p_out_InformacionDetalle			TABLE ("IdInterno"	BIGINT,"IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno"	BIGINT,"IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(1),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno"	BIGINT,"IdOperacion" BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
	
	
	
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL 
	READS SQL DATA AS
BEGIN
	
		
		v_TablaCodigosMensajes = 	SELECT "IdOperacion" AS "IdInterno","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2"
									FROM :p_SalidaDetalle det;
		
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT det."IdInterno",
										det."IdOperacion",
										det."Codigo",
										msj."Descripcion"
								FROM :p_SalidaDetalle det
								LEFT JOIN :v_out_ErroresDetalleMensajes msj on det."IdOperacion" = msj."IdInterno" AND det."Codigo" = msj."Codigo"
								where det."Resultado" in ('RECHAZO')
								ORDER BY det."IdInterno",det."IdOperacion";
								
		p_out_AdvertenciasDetalle = SELECT det."IdInterno",
										det."IdOperacion",
										det."Codigo",
										CASE det."Resultado" WHEN 'AUTORIZACION' THEN 'S' ELSE 'N' END AS "RequiereAutorizacion",
										msj."Descripcion"
									FROM :p_SalidaDetalle det
									LEFT JOIN :v_out_ErroresDetalleMensajes msj on det."IdOperacion" = msj."IdInterno" AND det."Codigo" = msj."Codigo"
									where det."Resultado" in ('ADVERTENCIA','AUTORIZACION')
									ORDER BY det."IdInterno",det."IdOperacion";
		
		p_out_InformacionDetalle = SELECT det."IdInterno",
										det."IdOperacion",
										det."Codigo",
										msj."Descripcion"
									FROM :p_SalidaDetalle det
									LEFT JOIN :v_out_ErroresDetalleMensajes msj on det."IdOperacion" = msj."IdInterno" AND det."Codigo" = msj."Codigo"
									where det."Resultado" in ('INFORMACION')
									ORDER BY det."IdInterno",det."IdOperacion";

		
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;
		select count(1) into p_out_InformacionContador 
		from :p_out_InformacionDetalle;
  
END;
 
