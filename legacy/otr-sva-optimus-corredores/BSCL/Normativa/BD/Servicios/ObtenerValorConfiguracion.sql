SET SCHEMA "BSCL";
--call "BSCL.Normativa.BD.Servicios.NCG380::ObtenerRutaArchivo"(?)
--  Proposito: Obtiene Ruta Parametrica de Archivos NCG380
--  Autor: JSALINAS - BCS
--  Fecha Creacion: 22/05/2016
--  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ObtenerValorConfiguracion" ;

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ObtenerValorConfiguracion"
(

  IN p_IdEmpresa          BIGINT default 0 ,
  IN p_CodigoParametro    NVARCHAR(500) --,

)  

	

	
	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA "BSCL" AS
	
	/*********BEGIN PROCEDURE SCRIPT ************/
	BEGIN

	select top 1 "Valor" from "BSCL.Normativa.BD::NOR.Configuracion" where "IdEmpresa" = p_IdEmpresa and "CodigoParametro" = p_CodigoParametro;
		
END;


