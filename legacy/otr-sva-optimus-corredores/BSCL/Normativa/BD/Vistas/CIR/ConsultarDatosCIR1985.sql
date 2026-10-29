SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Normativa.BD.Vistas::CIR.ConsultarDatosCIR1985";
GO
CREATE VIEW "BSCL.Normativa.BD.Vistas::CIR.ConsultarDatosCIR1985" AS
SELECT 
				 "CEO"."IdEjecucion"						
				,"CEO"."IdDocumento" 
				,"CC"."NombreCompleto"					
				,COALESCE("CPDF"."NombreLargo",'-')			AS "DireccionFisica"
				,"CC"."DocumentoIdentidad" 					AS "RutCliente"
				,"CC"."IdPersona"
				,"CPT"."Numero" 							AS "Telefono"
				,"CPDE"."IdDireccionElectronica" 			
				,"CPDE"."Direccion" 						AS "DireccionElectronica"
				,"CEO"."FolioGeneracion"					
				,"CEO"."IdOrden"							
				,"CEO"."Folio" 							
				,"CEO"."IdCliente" 						
				,"CEO"."IdPortafolio"
				,coalesce(POR."CodigoPortafolio",'') AS "CodigoPortafolio"
				,"CEO"."IdClasificacionTipoInstrumento" 	
				,"CEO"."IdTipoInstrumento"				
				,"CEO"."IdTipoOperacion"					
				,"CEO"."IdInstrumento" 					
				,"CEO"."Cantidad" 						
				,"CEO"."Monto" 							
				,"CEO"."MontoValorizado"					
				,"CEO"."Precio" 							
				,"CEO"."IdMoneda" 						
				,"CEO"."IdCondicionDeLiquidacion" 		
				,TO_VARCHAR(LEFT(UTCTOLOCAL(TO_TIMESTAMP("CEO"."FechaRecepcion",'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform'),16), 'DD/MM/YYYY HH24:MI')     as  "FechaRecepcion"			
				,TO_VARCHAR(LEFT(UTCTOLOCAL(TO_TIMESTAMP("CEO"."FechaAceptacion",'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform'),16),'DD/MM/YYYY HH24:MI')       as  "FechaAceptacion"	
				,"CEO"."TipoEjecucion"					
				,UTCTOLOCAL(TO_TIMESTAMP("CEO"."FechaEjecucion",'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform') as 	"FechaEjecucion"	
				,TO_VARCHAR(LEFT(UTCTOLOCAL(TO_TIMESTAMP("CEO"."FechaEjecucion",'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform'),16), 'DD-MM-YYYY HH24:MI')        as "FechaHoraEjecucion"
				,"CEO"."IdBolsa" 
				,"CEO"."EsOD"
				,CASE TRIM("CEO"."EsOD") WHEN  'S' THEN 'OD' ELSE 'OS' END   	"Modalidad"						
				,"CEO"."PrecioEjecucion"					
				,"CEO"."MontoTransado" 					
				,"CEO"."CantidadTransada"				
				,"CEO"."ComisionTransada"		
				--OTRAS BOLSAS 		
				,"CEO"."MejorPrecioOtraBolsa"			
				,"CEO"."CantidadMejorPrecioOtraBolsa" 	
				----MERCADO
				,CASE WHEN COALESCE("CEO"."HoraPrecioMinimoMercado",'') = '' OR COALESCE("CEO"."PrecioMinimoMercado",0) = 0 THEN '-/--:--/---'
				 ELSE "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(COALESCE("CEO"."PrecioMinimoMercado",0),'d',2,'CL') || '/' || TO_VARCHAR(UTCTOLOCAL(TO_TIMESTAMP(CONCAT(TO_VARCHAR("CEO"."FechaAceptacion",'YYYY-MM-DD '),"CEO"."HoraPrecioMinimoMercado"),'YYYY-MM-DD HH24:MI'),'America/Santiago','platform'),'HH24:MI') ||'/'|| TO_VARCHAR(COALESCE("CEO"."IdBolsaPrecioMinimoMercado",'BCS'))
				 END AS "PrecioMinHoraBolsaMcdo"
				,CASE WHEN COALESCE("CEO"."HoraPrecioMaximoMercado",'') = '' OR COALESCE("CEO"."PrecioMaximoMercado",0) = 0  THEN '-/--:--/---'
				 ELSE "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(COALESCE("CEO"."PrecioMaximoMercado",0),'d',2,'CL') || '/' || TO_VARCHAR(UTCTOLOCAL(TO_TIMESTAMP(CONCAT(TO_VARCHAR("CEO"."FechaAceptacion",'YYYY-MM-DD '),"CEO"."HoraPrecioMaximoMercado"),'YYYY-MM-DD HH24:MI'),'America/Santiago','platform'),'HH24:MI') ||'/'|| TO_VARCHAR(COALESCE("CEO"."IdBolsaPrecioMaximoMercado",'BCS'))
				 END AS  "PrecioMaxHoraBolsaMcdo"
				,"CEO"."MejorPrecioMercado"				
				,"CEO"."PrecioMinimoMercado"				
				,"CEO"."HoraPrecioMinimoMercado"			
				,"CEO"."IdBolsaPrecioMinimoMercado" 		
				,"CEO"."PrecioMaximoMercado"				
				,"CEO"."HoraPrecioMaximoMercado"			
				,"CEO"."IdBolsaPrecioMaximoMercado" 	
				---CORREDOR
				,CASE WHEN COALESCE("CEO"."HoraPrecioMenorCorredor",'') = '' OR COALESCE("CEO"."MenorPrecioOperacionCorredor",0) = 0 THEN '-/--:--'
				 ELSE "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(COALESCE("CEO"."MenorPrecioOperacionCorredor",0),'d',2,'CL')  || '/' || TO_VARCHAR(UTCTOLOCAL(TO_TIMESTAMP(CONCAT(TO_VARCHAR("CEO"."FechaAceptacion",'YYYY-MM-DD '),"CEO"."HoraPrecioMenorCorredor"),'YYYY-MM-DD HH24:MI'),'America/Santiago','platform'),'HH24:MI')
				 END AS  "PrecioMinHoraBolsaCorr"
				,CASE WHEN COALESCE("CEO"."HoraPrecioMayorCorredor",'') ='' OR COALESCE("CEO"."MayorPrecioOperacionCorredor",0) = 0 THEN '-/--:--'
				 ELSE "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(COALESCE("CEO"."MayorPrecioOperacionCorredor",0),'d',2,'CL') ||  '/' || TO_VARCHAR(UTCTOLOCAL(TO_TIMESTAMP(CONCAT(TO_VARCHAR("CEO"."FechaAceptacion",'YYYY-MM-DD '),"CEO"."HoraPrecioMayorCorredor"),'YYYY-MM-DD HH24:MI'),'America/Santiago','platform'),'HH24:MI') 
				 END AS  "PrecioMaxHoraBolsaCorr"
				,"CEO"."MayorPrecioOperacionCorredor"	
				,"CEO"."HoraPrecioMayorCorredor"			
				,"CEO"."MenorPrecioOperacionCorredor"	
				,"CEO"."HoraPrecioMenorCorredor"			
				,"CEO"."IdEmpleadoDigitador"				
				,"CEO"."FolioComprobanteFacturacion"     
				,"CEO"."FechaGeneracion" 				
				,"CEO"."FolioExterno" 					
				,"CEO"."IdEstadoNotificacion"		 	
				,"CEO"."IdEstadoCarta"		 			 
				,"CEO"."FechaIngreso"      				
				,"CEO"."FechaCreacion"     				
				,"CEO"."IdUsuarioCreador" 
				,"CEO"."FechaReferencia"
				,EMP."NombreCompleto" AS "NombreAgente"
				,TIPOOPERACION."Accion" 						AS "AccionOperacion"
				,TERE."Numero" || '  '||TERE."Anexo" AS "TelefonoAgente"
				,INS."Nemotecnico" as "Nemotecnico"
				,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(COALESCE("BEC"."PrecioMenor",0),'d',2,'CL') || '/' || COALESCE(TO_VARCHAR(UTCTOLOCAL(TO_TIMESTAMP(CONCAT(TO_VARCHAR("CEO"."FechaAceptacion",'YYYY-MM-DD '),"BEC"."HoraPrecioMinimo"),'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform'),'HH24:MI'),'00:00') || '/BEC'
					AS "PrecioMinHoraBolsaBEC"
				,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(COALESCE("BEC"."PrecioMayor",0),'d',2,'CL') || '/' || COALESCE(TO_VARCHAR(UTCTOLOCAL(TO_TIMESTAMP(CONCAT(TO_VARCHAR("CEO"."FechaAceptacion",'YYYY-MM-DD '),"BEC"."HoraPrecioMaximo"),'YYYY-MM-DD HH24:MI:SS'),'America/Santiago','platform'),'HH24:MI'),'00:00') || '/BEC'
				 	AS  "PrecioMaxHoraBolsaBEC"
				,COALESCE("ORD"."Folio",'0') AS "FolioOrden"	 
				FROM "BSCL.Normativa.BD::EJORD.EjecucionOrdenes" AS "CEO"	
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CC" ON ("CC"."IdCliente" = "CEO"."IdCliente")
				LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMP ON ("CC"."IdEmpleadoEjecutivo" =EMP."IdEmpleado")
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TERE	ON (EMP."IdTelefonoPrincipal"=TERE."IdTelefono")
				LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON ("CEO"."IdPortafolio"=POR."IdPortafolio")
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" AS "CPDF" ON ("CPDF"."IdDireccionFisica" = "CC"."IdDireccionFisicaPrincipal")
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" AS "CPDE" ON ("CPDE"."IdDireccionElectronica" = "CC"."IdDireccionElectronicaPrincipal")
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" AS "CPT" ON ("CPT"."IdTelefono" = "CC"."IdTelefonoPrincipal")
				LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION	ON TIPOOPERACION."IdTipoOperacion" = "CEO"."IdTipoOperacion"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON "CEO"."IdInstrumento" = INS."IdInstrumento"
				LEFT JOIN "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" AS BEC ON "CEO"."IdInstrumento" = "BEC"."IdInstrumento" AND "BEC"."IdBolsa" = 'BEC' AND   TO_DATE("CEO"."FechaEjecucion") >=  "BEC"."Fecha" AND TO_DATE("CEO"."FechaEjecucion") <=  "BEC"."Fecha"
				LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS "ORD" ON "CEO"."IdOrden" = "ORD"."IdOrden";
GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CIR.ConsultarDatosCIR1985" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


