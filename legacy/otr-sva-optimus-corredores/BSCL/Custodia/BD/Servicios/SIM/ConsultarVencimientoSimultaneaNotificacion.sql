SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::SIM.ConsultarVencimientoSimultaneaNotificacion";
GO

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::SIM.ConsultarVencimientoSimultaneaNotificacion"
( 
          IN 	p_IdEmpresa					BIGINT DEFAULT 0,
          OUT   p_out_resultado				NVARCHAR(500) 

)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  
   

DECLARE v_FecSistema 			NVARCHAR(10) DEFAULT '0001-01-01';
DECLARE v_FechaAvisoVencimiento NVARCHAR(10) DEFAULT '0001-01-01';
DECLARE v_DiasVencimiento   	BIGINT:=0;
DECLARE v_CondSimVencidas   	BIGINT:=0;
DECLARE v_Resultado			   	BIGINT:=0;


DECLARE p_Eventos    			
TABLE(
 "CodigoTipoEvento"			NVARCHAR(40),
 "IdSecuencia"			    BIGINT,
 "FechaCreacionSistema"     SECONDDATE,
 "IdPersonaRemitente"		BIGINT,
 "IdPersonaDestinatario"	BIGINT,
 "IdPortafolio"				BIGINT);
 
 DECLARE p_EventoDetalle 
 TABLE(
 "IdSecuencia" BIGINT, 
 "Llave" NVARCHAR(40), 
 "Valor" NVARCHAR(5000));
 
 
 DECLARE  p_ArchivosAdjuntos 
 TABLE(
 "IdSecuencia" BIGINT,
 "ArchivoAdjunto" BLOB,
 "Nombre" NVARCHAR(520),
 "Contrasenia" NVARCHAR(520));
 
 DECLARE p_IdDirecciones
 TABLE(
 "IdSecuencia" BIGINT,
 "IdDireccion" BIGINT,
 "TipoDestinatario" NVARCHAR(6));
 
 DECLARE p_out_IdEvento	TABLE("IdEvento" BIGINT,"IdSecuencia" BIGINT);

		
		select '' into p_out_resultado from dummy;

		
		select "Valor" into v_DiasVencimiento from "BSCL.Custodia.BD::CUS.Configuracion"
		where "IdEmpresa" = 1 and "CodigoParametro" = 'DiasVencimientoSimultanea';
		
		select Top 1 "Valor" into v_FecSistema from "BSCL.Plataforma.BD::PLA.Configuracion" where "CodigoParametro" = 'PLA.FechaSistema' and "IdEmpresa" = p_IdEmpresa ;
		
		select ADD_DAYS(TO_DATE (v_FecSistema, 'YYYY-MM-DD'), v_DiasVencimiento) into v_FechaAvisoVencimiento from dummy;
		
		--select  v_FechaAvisoVencimiento from dummy;
		
		
		select count(1) into v_CondSimVencidas from 
		"BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaVigentes";
		--where "FechaVencimientoOperacion" = v_FechaAvisoVencimiento;
		
		
		
			
		IF(v_CondSimVencidas>0)
		THEN
		v_InfoNotificacion =
			SELECT
			ROW_NUMBER()over ( order by "SIM"."IdCliente","SIM"."IdPortafolio") AS "IdInterno"
			,"SIM"."IdCliente"
			,"SIM"."IdPersona"
			,"SIM"."IdPortafolio"
			,"SIM"."IdDireccionElectronicaPrincipal"
			,"SIM"."NombreLargoCliente"
			,"SIM"."FolioCartera"
			,"SIM"."FechaVencimientoOperacion"
/*			,"SIM"."AccionOperacion"
			,"SIM"."IdPortafolio"
			,"SIM"."IdMonedaSistema"
			,"SIM"."IdMonedaInstrumento"
			,"SIM"."INGCantidad"
			,"SIM"."INGTasaCompra"
			,"SIM"."INGPrecioCompra"
			
*/
			FROM
				
			(SELECT 
			"CCSV"."IdCliente"
			,"CCSV"."IdPortafolio" 			as "IdPortafolio"
			,"CLI"."IdPersona" 				as "IdPersona"
			,1938138 						as "IdDireccionElectronicaPrincipal"
			--,"CLI"."IdDireccionElectronicaPrincipal" as "IdDireccionElectronicaPrincipal"
			,"CCSV"."NombreLargoCliente" 	as "NombreLargoCliente"
			,"CCSV"."FolioCartera" 			as "FolioCartera"
			,TO_VARCHAR ("CCSV"."FechaVencimientoOperacion", 'YYYY-MM-DD') as "FechaVencimientoOperacion"

			--,TO_DATE("FechaVencimientoOperacion",'YYYY-MM-DD') as "FechaVencimientoOperacion"
			--,"CCSV"."FechaVencimientoOperacion"
/*			,"CCSV"."AccionOperacion"
			,"CCSV"."IdPortafolio"
			,"CCSV"."IdMonedaSistema"
			,"CCSV"."IdMonedaInstrumento"
			,"CCSV"."INGCantidad"
			,"CCSV"."INGTasaCompra"
			,"CCSV"."INGPrecioCompra"
*/
			FROM
			"BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaVigentes" AS "CCSV"
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CLI"
			ON ("CLI"."IdCliente"="CCSV"."IdCliente")
			where "CCSV"."FechaVencimientoOperacion" = "CCSV"."FechaVencimientoOperacion"--v_FechaAvisoVencimiento
			GROUP BY  "CCSV"."IdCliente","CCSV"."IdPortafolio","CLI"."IdPersona","CLI"."IdDireccionElectronicaPrincipal","CCSV"."NombreLargoCliente","CCSV"."FolioCartera","CCSV"."FechaVencimientoOperacion"
			 
			
			)as "SIM"
			;
	
		
		ELSE
		v_InfoNotificacion =
			SELECT
			 0  AS "IdInterno"
			,0	 	as "IdCliente"
			,0	 	as "IdPersona"
			,0	 	as "IdPortafolio"
			,'' 	as "IdDireccionElectronicaPrincipal"
			,'' 	as "NombreLargoCliente"
			,0 		as "FolioCartera"
			,'0001-01-01' as "FechaVencimientoOperacion"
/*			
			,'' 	as "AccionOperacion"
			,0 		as "IdPortafolio"
			,'' 	as "IdMonedaSistema"
			,'' 	as "IdMonedaInstrumento"
			,0 		as "INGCantidad"
			,0 		as "INGTasaCompra"
			,0 		as "INGPrecioCompra"
*/
			FROM DUMMY WHERE 1=2;
		
		END IF;
		
		IF(v_CondSimVencidas>0)
		THEN
		
				p_Eventos = 
				 SELECT 
				'SIMVENCIDOS_CLIENTE' 				AS "CodigoTipoEvento", 
				"INF"."IdInterno" 					AS "IdSecuencia", 
				CURRENT_UTCTIMESTAMP 				AS "FechaCreacionSistema", 
				0 									AS "IdPersonaRemitente", 
				"INF"."IdPersona" 					AS "IdPersonaDestinatario", 
				"INF"."IdPortafolio" 				AS "IdPortafolio" 
				FROM  :v_InfoNotificacion as "INF";
								
				p_IdDirecciones =
				SELECT "INF"."IdInterno"					 	AS "IdSecuencia",
					"INF"."IdDireccionElectronicaPrincipal" 	AS "IdDireccion",
					'TO' 								  		AS "TipoDestinatario"
				FROM  :v_InfoNotificacion "INF";
				
				p_ArchivosAdjuntos =
				SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;
				
				--Datos del Cliente en mail
											
			p_EventoDetalle = 
				SELECT 
				"INF"."IdInterno" AS "IdSecuencia", 
				'NombreLargoCliente' AS "Llave",  
				"INF"."NombreLargoCliente" AS "Valor" 
				FROM :v_InfoNotificacion "INF"
				UNION
				SELECT 
				"INF"."IdInterno" AS "IdSecuencia", 
				'FechaVencimientoOperacion' AS "Llave",  
				"INF"."FechaVencimientoOperacion" AS "Valor" 
				FROM :v_InfoNotificacion "INF"
				;

	--	select * from :p_Eventos;
	--	select * from :p_EventoDetalle;
	--	select * from :p_IdDirecciones;
	--	select * from :p_ArchivosAdjuntos;

		--Guardar eventos
			CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(
 				p_IdEmpresa => p_IdEmpresa,
 				p_IdUsuario => 14,
 				p_IdModulo => 'CUS',
 				p_Eventos => :p_Eventos,
 				p_Valores => :p_EventoDetalle,
 				p_ArchivosAdjuntos => :p_ArchivosAdjuntos,
 				p_IdDirecciones => :p_IdDirecciones,
 				p_out_IdEvento => p_out_IdEvento
 			);



		select * from :p_out_IdEvento;
		select 1 into v_Resultado from dummy;
		
		
		

		END IF;


IF(v_Resultado>0)
THEN

select 'Finalizado Correctamente' into p_out_resultado from dummy;


ELSE

IF(v_CondSimVencidas=0)
THEN

select 'Sin Registros' into p_out_resultado from dummy;
END IF;


END IF;


END;
