SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::CrearSerieDeFondo";
GO
CREATE PROCEDURE
--  Proposito: Crear Serie de Fondo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 24/06/2016
"BSCL.FondoMutuo.BD.Servicios::CrearSerieDeFondo" (
	 --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 				BIGINT,
    
	IN p_SerieFondo 			TABLE	(
			    					  		"IdInterno" 						 BIGINT,
			    					  		"IdSerieFondo"						 NVARCHAR(40),
			    					  		"IdInstrumento"						 BIGINT,
			    					  		"IdFondo"							 NVARCHAR(40),
			    					  		"NombreSerieFondo"					 NVARCHAR(100),
			    					  		"CodigoSerie"						 NVARCHAR(10),
			    					  		"Alias"								 NVARCHAR(40),
			    					  		"DecimalesValorCuota"		  		 BIGINT		,
	 										"DecimalesCuota"			  		 BIGINT		,
			    					  		"MontoMinimo"						 DECIMAL(28,6),
			    					  		"MontoMaximo"						 DECIMAL(28,6),
			    					  		"DiasRetencionesAporte"              BIGINT,
			    					  		"DiasRetencionesRescate"			 BIGINT,
			    					  		"ExcluyeGarantia"					 NVARCHAR(1),
			    					  		"CobraComision"			  			 NVARCHAR(1),
			    					  		"Diferido"					  		 NVARCHAR(1),
			    					  		"InstrumentoDCV"			  		 NVARCHAR(40),
			    					  		"Omnibus"					  		 NVARCHAR(1),
			    					  		"PatrimonioMinimo"					 DECIMAL(28,6),
			    					  		"IdSerieFondoOrigen"				 NVARCHAR(40),
			    					  		"IdCliente"							 BIGINT,
			    					  		"IdPortafolio"						 BIGINT,
			    					  		"IdMedioDePagoAbono"				 NVARCHAR(3),
			    					  		"IdMedioDePagoCargo"				 NVARCHAR(3),
			    					  		"DiasValutaValorCuotaAporte"         BIGINT,
                                            "DiasValutaValorCuotaRescate"        BIGINT,
			    					  		"DiasValutaCertificacionAporte" 	 BIGINT,
			    					  		"DiasValutaCertificacionRescate" 	 BIGINT,
			    					  		"DiasValutaCurseAporte"				 BIGINT,
	 										"DiasValutaCurseRescate"			 BIGINT,
	 										"IdFamiliaFondo"					 NVARCHAR(40),
			    					  		"IdEstado"							 NVARCHAR(3),
			    					  		"CambioFondo"   					 NVARCHAR(1),
			    					  		"DiasComision"      				 BIGINT,
									  		"PorcentajeComision"                 DECIMAL(26,8),
									  		"PorcentajeRemuneracion"             DECIMAL(26,8),
									  		"PorcentajeGastoOpera"          	 DECIMAL(26,8),
									  		"PorcentajeCostoAdm"          		 DECIMAL(26,8),
									  		"AportesAltos"						 NVARCHAR(1),
									  		"PatrimonioAltos"					 NVARCHAR(1),
										  	"MontoPatrimonioEmisor"				 DECIMAL(26,8),
										  	"InstrumentoOWP"					 NVARCHAR(1),
										  	"EsAPV"								 NVARCHAR(1),
											"NemoCMF"							 NVARCHAR(40)
		    					  		),
  	
  	OUT p_out_Resultado			TABLE	(
  											"IdInterno"							 BIGINT,
  											"IdSerieFondo"						 NVARCHAR(40),
  											"IdInstrumento"						 BIGINT,
  											"IdFondo"							 NVARCHAR(40),
  											"NombreSerieFondo"					 NVARCHAR(100),
  											"CodigoSerie"						 NVARCHAR(10),
  											"Alias"								 NVARCHAR(40),
  											"DecimalesValorCuota"		  		 BIGINT,
	 										"DecimalesCuota"			  		 BIGINT,
  											"MontoMinimo"						 DECIMAL(28,6),
  											"MontoMaximo"						 DECIMAL(28,6),
  											"DiasRetencionesAporte"              BIGINT,
  											"DiasRetencionesRescate"			 BIGINT,
  											"ExcluyeGarantia"					 NVARCHAR(1),
  											"CobraComision"			  			 NVARCHAR(1),
			    					  		"Diferido"					  		 NVARCHAR(1),
			    					  		"InstrumentoDCV"			  		 NVARCHAR(40),
			    					  		"Omnibus"					  		 NVARCHAR(1),
  											"PatrimonioMinimo"					 DECIMAL(28,6),
  											"IdSerieFondoOrigen"				 NVARCHAR(40),
  											"IdEstado"							 NVARCHAR(3),
  											"FechaIngresoRegistro"				 TIMESTAMP,
  											"IdCliente"							 BIGINT,
			    					  		"IdPortafolio"						 BIGINT,
			    					  		"IdMedioDePagoAbono"				 NVARCHAR(3),
			    					  		"IdMedioDePagoCargo"				 NVARCHAR(3),
			    					  		"DiasValutaValorCuotaAporte"         BIGINT,
                                            "DiasValutaValorCuotaRescate"        BIGINT,
			    					  		"DiasValutaCertificacionAporte" 	 BIGINT,
			    					  		"DiasValutaCertificacionRescate" 	 BIGINT,
			    					  		"DiasValutaCurseAporte"				 BIGINT,
	 										"DiasValutaCurseRescate"			 BIGINT,
	 										"IdFamiliaFondo"					 NVARCHAR(40),
	 										"CambioFondo"   					 NVARCHAR(1),
  											"IdEmpresa"							 BIGINT,
  											"DiasComision"      				 BIGINT,
									  		"PorcentajeComision"                 DECIMAL(26,8),
									  		"PorcentajeRemuneracion"             DECIMAL(26,8),
									  		"PorcentajeGastoOpera"          	 DECIMAL(26,8),
									  		"PorcentajeCostoAdm"          		 DECIMAL(26,8),
									  		"AportesAltos"						 NVARCHAR(1),
									  		"PatrimonioAltos"					 NVARCHAR(1),
										  	"MontoPatrimonioEmisor"				 DECIMAL(26,8),
										  	"InstrumentoOWP"                     NVARCHAR(1),
										  	"EsAPV"                              NVARCHAR(1),
											"NemoCMF"							 NVARCHAR(40)
  										)
	)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	BEGIN
	DECLARE v_FechaSistema 			NVARCHAR(10);

	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
	v_series_maestro = SELECT
							"P_SerieDeFondo"."IdInterno"
							,"P_SerieDeFondo"."IdSerieFondo"
							,"P_SerieDeFondo"."IdInstrumento"					
					  		,"P_SerieDeFondo"."IdFondo"						
					  		,"P_SerieDeFondo"."NombreSerieFondo"				
					  		,"P_SerieDeFondo"."CodigoSerie"								
					  		,"P_SerieDeFondo"."Alias"	
					  		,"P_SerieDeFondo"."DecimalesValorCuota"
					  		,"P_SerieDeFondo"."DecimalesCuota"					
					  		,"P_SerieDeFondo"."MontoMinimo"					
					  		,"P_SerieDeFondo"."MontoMaximo"
					  		,"P_SerieDeFondo"."DiasRetencionesAporte"
					  		,"P_SerieDeFondo"."DiasRetencionesRescate"	
					  		,"P_SerieDeFondo"."ExcluyeGarantia"
					  		,"P_SerieDeFondo"."CobraComision"
					  		,"P_SerieDeFondo"."Diferido"
					  		,"P_SerieDeFondo"."InstrumentoDCV"
					  		,"P_SerieDeFondo"."Omnibus"					
					  		,"P_SerieDeFondo"."PatrimonioMinimo"				
					  		,"P_SerieDeFondo"."IdSerieFondoOrigen"
					  		,"P_SerieDeFondo"."IdCliente"
					  		,"P_SerieDeFondo"."IdPortafolio"
					  		,"P_SerieDeFondo"."IdMedioDePagoAbono"
					  		,"P_SerieDeFondo"."IdMedioDePagoCargo"
					  		,"P_SerieDeFondo"."DiasValutaValorCuotaAporte"
                            ,"P_SerieDeFondo"."DiasValutaValorCuotaRescate"
					  		,"P_SerieDeFondo"."DiasValutaCertificacionAporte"
					  		,"P_SerieDeFondo"."DiasValutaCertificacionRescate"
					  		,"P_SerieDeFondo"."DiasValutaCurseAporte"
					  		,"P_SerieDeFondo"."DiasValutaCurseRescate"		
					  		,"P_SerieDeFondo"."IdEstado"
					  		,"P_SerieDeFondo"."IdFamiliaFondo"						
					  		,CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro"
					  		,"P_SerieDeFondo"."CambioFondo"
					  		,:p_IdEmpresa AS "IdEmpresa"
					  		,"P_SerieDeFondo"."DiasComision"
					  		,"P_SerieDeFondo"."PorcentajeComision"
					  		,"P_SerieDeFondo"."PorcentajeRemuneracion"
					  		,"P_SerieDeFondo"."PorcentajeGastoOpera"
					  		,"P_SerieDeFondo"."PorcentajeCostoAdm"
					  		,"P_SerieDeFondo"."AportesAltos"
					  		,"P_SerieDeFondo"."PatrimonioAltos"
							,"P_SerieDeFondo"."MontoPatrimonioEmisor"
							,"P_SerieDeFondo"."InstrumentoOWP"
							,"P_SerieDeFondo"."EsAPV"
							,"P_SerieDeFondo"."NemoCMF"
					  	FROM :p_SerieFondo AS "P_SerieDeFondo";
	
	 
	 v_series_finales = SELECT
	 						"SerieDeFondoMaestro"."IdInterno"
							,"SerieDeFondoMaestro"."IdSerieFondo"
							,"SerieDeFondoMaestro"."IdInstrumento"					
					  		,"SerieDeFondoMaestro"."IdFondo"						
					  		,"SerieDeFondoMaestro"."NombreSerieFondo"				
					  		,"SerieDeFondoMaestro"."CodigoSerie"								
					  		,"SerieDeFondoMaestro"."Alias"
					  		,"SerieDeFondoMaestro"."DecimalesValorCuota"
					  		,"SerieDeFondoMaestro"."DecimalesCuota"						
					  		,"SerieDeFondoMaestro"."MontoMinimo"					
					  		,"SerieDeFondoMaestro"."MontoMaximo"
					  		,"SerieDeFondoMaestro"."DiasRetencionesAporte"
					  		,"SerieDeFondoMaestro"."DiasRetencionesRescate"			
					  		,"SerieDeFondoMaestro"."ExcluyeGarantia"
					  		,"SerieDeFondoMaestro"."CobraComision"
					  		,"SerieDeFondoMaestro"."Diferido"
					  		,"SerieDeFondoMaestro"."InstrumentoDCV"
					  		,"SerieDeFondoMaestro"."Omnibus"					
					  		,"SerieDeFondoMaestro"."PatrimonioMinimo"				
					  		,"SerieDeFondoMaestro"."IdSerieFondoOrigen"			
					  		,"SerieDeFondoMaestro"."IdEstado"
					  		,"SerieDeFondoMaestro"."FechaIngresoRegistro"
					  		,"SerieDeFondoMaestro"."IdCliente"
					  		,"SerieDeFondoMaestro"."IdPortafolio"
					  		,"SerieDeFondoMaestro"."IdMedioDePagoAbono"
					  		,"SerieDeFondoMaestro"."IdMedioDePagoCargo"
					  		,"SerieDeFondoMaestro"."DiasValutaValorCuotaAporte"
                            ,"SerieDeFondoMaestro"."DiasValutaValorCuotaRescate"
					  		,"SerieDeFondoMaestro"."DiasValutaCertificacionAporte"
					  		,"SerieDeFondoMaestro"."DiasValutaCertificacionRescate"
					  		,"SerieDeFondoMaestro"."DiasValutaCurseAporte"
					  		,"SerieDeFondoMaestro"."DiasValutaCurseRescate"
					  		,"SerieDeFondoMaestro"."IdFamiliaFondo"
					  		,"SerieDeFondoMaestro"."CambioFondo"
					  		,"SerieDeFondoMaestro"."IdEmpresa"
					  		,"SerieDeFondoMaestro"."DiasComision"
					  		,"SerieDeFondoMaestro"."PorcentajeComision"
					  		,"SerieDeFondoMaestro"."PorcentajeRemuneracion"
					  		,"SerieDeFondoMaestro"."PorcentajeGastoOpera"
					  		,"SerieDeFondoMaestro"."PorcentajeCostoAdm"
					  		,"SerieDeFondoMaestro"."AportesAltos"
							,"SerieDeFondoMaestro"."PatrimonioAltos"
							,"SerieDeFondoMaestro"."MontoPatrimonioEmisor"
							,"SerieDeFondoMaestro"."InstrumentoOWP"
							,"SerieDeFondoMaestro"."EsAPV"
							,"SerieDeFondoMaestro"."NemoCMF"
					  	FROM :v_series_maestro AS "SerieDeFondoMaestro";
	
	
					  	
	INSERT INTO "BSCL.FondoMutuo.BD::FMM.SerieDeFondo"(
		"IdSerieFondo", "IdInstrumento", "IdFondo", "NombreSerieFondo", "CodigoSerie", "Alias", "DecimalesValorCuota", "DecimalesCuota", "MontoMinimo",				
		"MontoMaximo", "DiasRetencionesAporte", "DiasRetencionesRescate", "ExcluyeGarantia", "CobraComision", "Diferido",
		"InstrumentoDCV", "Omnibus", "PatrimonioMinimo", "IdSerieFondoOrigen", "IdEstado", "FechaIngresoRegistro", "IdCliente", "IdPortafolio", "IdMedioDePagoAbono",
		"IdMedioDePagoCargo", "DiasValutaValorCuotaAporte", "DiasValutaValorCuotaRescate", "DiasValutaCertificacionAporte", "DiasValutaCertificacionRescate", "DiasValutaCurseAporte",
		"DiasValutaCurseRescate", "IdFamiliaFondo","CambioFondo", "IdEmpresa","DiasComision","PorcentajeComision","PorcentajeRemuneracion",
		"PorcentajeGastoOpera","PorcentajeCostoAdm","AportesAltos","PatrimonioAltos","MontoPatrimonioEmisor","InstrumentoOWP","EsAPV","NemoCMF")
	SELECT "IdSerieFondo", "IdInstrumento", "IdFondo", "NombreSerieFondo", "CodigoSerie", "Alias", "DecimalesValorCuota", "DecimalesCuota", "MontoMinimo",				
		"MontoMaximo", "DiasRetencionesAporte", "DiasRetencionesRescate", "ExcluyeGarantia", "CobraComision", "Diferido",
		"InstrumentoDCV", "Omnibus", "PatrimonioMinimo", "IdSerieFondoOrigen", "IdEstado", "FechaIngresoRegistro", "IdCliente", "IdPortafolio", "IdMedioDePagoAbono",
		"IdMedioDePagoCargo", "DiasValutaValorCuotaAporte", "DiasValutaValorCuotaRescate", "DiasValutaCertificacionAporte", "DiasValutaCertificacionRescate", "DiasValutaCurseAporte", 
		"DiasValutaCurseRescate", "IdFamiliaFondo","CambioFondo", "IdEmpresa","DiasComision","PorcentajeComision","PorcentajeRemuneracion",
		"PorcentajeGastoOpera","PorcentajeCostoAdm","AportesAltos","PatrimonioAltos","MontoPatrimonioEmisor","InstrumentoOWP","EsAPV","NemoCMF"
	FROM :v_series_finales;
	
	p_out_Resultado = SELECT 	"IdInterno",
								"IdSerieFondo",
								"IdInstrumento", 
								"IdFondo", 
								"NombreSerieFondo", 
								"CodigoSerie", 
								"Alias",
								"DecimalesValorCuota",
								"DecimalesCuota", 
								"MontoMinimo",				
								"MontoMaximo",
								"DiasRetencionesAporte",
								"DiasRetencionesRescate", 
								"ExcluyeGarantia", 
								"CobraComision",
								"Diferido",
								"InstrumentoDCV",
								"Omnibus",
								"PatrimonioMinimo",				
								"IdSerieFondoOrigen", 
								"IdEstado", 
								"FechaIngresoRegistro",
								"IdCliente",
								"IdPortafolio",
								"IdMedioDePagoAbono",
								"IdMedioDePagoCargo",
								"DiasValutaValorCuotaAporte",
								"DiasValutaValorCuotaRescate", 
								"DiasValutaCertificacionAporte",
								"DiasValutaCertificacionRescate",
								"DiasValutaCurseAporte",
								"DiasValutaCurseRescate",
								"IdFamiliaFondo",
								"CambioFondo",
								"IdEmpresa",
								"DiasComision",
								"PorcentajeComision",
								"PorcentajeRemuneracion",
								"PorcentajeGastoOpera",
								"PorcentajeCostoAdm",
								"AportesAltos",
								"PatrimonioAltos",
								"MontoPatrimonioEmisor",
								"InstrumentoOWP",
								"EsAPV",
								"NemoCMF"
						FROM :v_series_finales;
	END;
END;
GO