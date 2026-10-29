SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Servicios::ModificarSerieDeFondo";
GO
CREATE PROCEDURE
--  Proposito: Modificar Serie de Fondo
--  Autor: - Gat-Blac 
--  Fecha Creacion: 28/06/2016
"BSCL.FondoMutuo.BD.Servicios::ModificarSerieDeFondo"(
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
										  	"RestringeAporte"					 NVARCHAR(1),
										  	"RestringeRescate"					 NVARCHAR(1),
											"NemoCMF"              				 NVARCHAR(40)
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
										  	"InstrumentoOWP"					 NVARCHAR(1),
										  	"EsAPV"								 NVARCHAR(1),
										  	"RestringeAporte"					 NVARCHAR(1),
										  	"RestringeRescate"					 NVARCHAR(1),
											"NemoCMF" 							 NVARCHAR(40)
  										)
	)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN	
	BEGIN
	DECLARE v_resultadoRegla 		NVARCHAR(40);
	
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
					  		,"P_SerieDeFondo"."IdFamiliaFondo"		
					  		,"P_SerieDeFondo"."IdEstado"
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
							,"P_SerieDeFondo"."RestringeAporte"					
							,"P_SerieDeFondo"."RestringeRescate"
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
							,"SerieDeFondoMaestro"."RestringeAporte"					
							,"SerieDeFondoMaestro"."RestringeRescate"
							,"SerieDeFondoMaestro"."NemoCMF"
					  	FROM :v_series_maestro AS "SerieDeFondoMaestro";
	
	UPDATE SerieFondo
		SET 
			"IdFondo" 					 		 = Serie_Finales."IdFondo",
			"IdInstrumento"				 		 = Serie_Finales."IdInstrumento",
			"NombreSerieFondo" 			 		 = Serie_Finales."NombreSerieFondo",
			"CodigoSerie" 				 		 = Serie_Finales."CodigoSerie",
			"Alias" 					 		 = Serie_Finales."Alias",
			"DecimalesValorCuota" 		 		 = Serie_Finales."DecimalesValorCuota",
			"DecimalesCuota" 			 		 = Serie_Finales."DecimalesCuota",
			"MontoMinimo" 				 		 = Serie_Finales."MontoMinimo",
			"MontoMaximo" 				 		 = Serie_Finales."MontoMaximo",
			"DiasRetencionesAporte"              = Serie_Finales."DiasRetencionesAporte",
			"DiasRetencionesRescate" 	 		 = Serie_Finales."DiasRetencionesRescate",
			"ExcluyeGarantia" 			 		 = Serie_Finales."ExcluyeGarantia",
			"CobraComision" 			 		 = Serie_Finales."CobraComision",
			"Diferido" 					 		 = Serie_Finales."Diferido",
			"InstrumentoDCV" 			 		 = Serie_Finales."InstrumentoDCV",
			"Omnibus" 					 		 = Serie_Finales."Omnibus",
			"PatrimonioMinimo" 			 		 = Serie_Finales."PatrimonioMinimo",
			"IdSerieFondoOrigen" 		 		 = Serie_Finales."IdSerieFondoOrigen",
			"IdCliente"					 		 = Serie_Finales."IdCliente",
			"IdPortafolio"				 		 = Serie_Finales."IdPortafolio",
			"IdMedioDePagoAbono"		 		 = Serie_Finales."IdMedioDePagoAbono",
			"IdMedioDePagoCargo"		 		 = Serie_Finales."IdMedioDePagoCargo",
            "DiasValutaValorCuotaAporte"      = Serie_Finales."DiasValutaValorCuotaAporte",
            "DiasValutaValorCuotaRescate"      = Serie_Finales."DiasValutaValorCuotaRescate",
			"DiasValutaCertificacionAporte" 	 = Serie_Finales."DiasValutaCertificacionAporte",
			"DiasValutaCertificacionRescate" 	 = Serie_Finales."DiasValutaCertificacionRescate",
			"DiasValutaCurseAporte"				 = Serie_Finales."DiasValutaCurseAporte",
			"DiasValutaCurseRescate"			 = Serie_Finales."DiasValutaCurseRescate",
			"IdFamiliaFondo"					 = Serie_Finales."IdFamiliaFondo",
			"IdEstado"		 			 		 = Serie_Finales."IdEstado",
			"CambioFondo"		 			 	 = Serie_Finales."CambioFondo",
			"DiasComision"						 = Serie_Finales."DiasComision",
			"PorcentajeComision"				 = Serie_Finales."PorcentajeComision",
			"PorcentajeRemuneracion"			 = Serie_Finales."PorcentajeRemuneracion",
			"PorcentajeGastoOpera"				 = Serie_Finales."PorcentajeGastoOpera",
			"PorcentajeCostoAdm"				 = Serie_Finales."PorcentajeCostoAdm",
			"AportesAltos"						 = Serie_Finales."AportesAltos",
			"PatrimonioAltos"					 = Serie_Finales."PatrimonioAltos",
			"MontoPatrimonioEmisor"				 = Serie_Finales."MontoPatrimonioEmisor",
			"InstrumentoOWP"					 = Serie_Finales."InstrumentoOWP",
			"EsAPV"								 = Serie_Finales."EsAPV",
			"RestringeAporte"					 = Serie_Finales."RestringeAporte",
			"RestringeRescate"					 = Serie_Finales."RestringeRescate",
			"NemoCMF"							 = Serie_Finales."NemoCMF"
	FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieFondo
	INNER JOIN :v_series_maestro AS Serie_Finales ON (SerieFondo."IdSerieFondo" = Serie_Finales."IdSerieFondo")
	WHERE SerieFondo."IdEmpresa" = Serie_Finales."IdEmpresa";
	
	p_out_Resultado = SELECT 	"SerieFondo"."IdInterno",
								"SerieFondo"."IdSerieFondo",
								"SerieFondo"."IdInstrumento", 
								"SerieFondo"."IdFondo", 
								"SerieFondo"."NombreSerieFondo", 
								"SerieFondo"."CodigoSerie", 
								"SerieFondo"."Alias",
								"SerieFondo"."DecimalesValorCuota",
					  			"SerieFondo"."DecimalesCuota", 
								"SerieFondo"."MontoMinimo",				
								"SerieFondo"."MontoMaximo", 
                                "SerieFondo"."DiasRetencionesAporte",
								"SerieFondo"."DiasRetencionesRescate", 
								"SerieFondo"."ExcluyeGarantia",
								"SerieFondo"."CobraComision",
					  			"SerieFondo"."Diferido",
					  			"SerieFondo"."InstrumentoDCV",
					  			"SerieFondo"."Omnibus", 
								"SerieFondo"."PatrimonioMinimo",				
								"SerieFondo"."IdSerieFondoOrigen", 
								"SerieFondo"."IdEstado", 
								"SerieFondo_Sis"."FechaIngresoRegistro",
								"SerieFondo"."IdCliente",
								"SerieFondo"."IdPortafolio",
								"SerieFondo"."IdMedioDePagoAbono",
								"SerieFondo"."IdMedioDePagoCargo",
                                "SerieFondo"."DiasValutaValorCuotaAporte",
                                "SerieFondo"."DiasValutaValorCuotaRescate",
								"SerieFondo"."DiasValutaCertificacionAporte",
								"SerieFondo"."DiasValutaCertificacionRescate",
								"SerieFondo"."DiasValutaCurseAporte",
					  			"SerieFondo"."DiasValutaCurseRescate",
					  			"SerieFondo"."IdFamiliaFondo",	
					  			"SerieFondo"."CambioFondo",
								"SerieFondo"."IdEmpresa",
								"SerieFondo"."DiasComision",
								"SerieFondo"."PorcentajeComision",
								"SerieFondo"."PorcentajeRemuneracion",
								"SerieFondo"."PorcentajeGastoOpera",
								"SerieFondo"."PorcentajeCostoAdm",
								"SerieFondo"."AportesAltos",
								"SerieFondo"."PatrimonioAltos",
								"SerieFondo"."MontoPatrimonioEmisor",
								"SerieFondo"."InstrumentoOWP",
								"SerieFondo"."EsAPV",
								"SerieFondo"."RestringeAporte",
								"SerieFondo"."RestringeRescate",
								"SerieFondo"."NemoCMF"
						FROM :v_series_finales AS "SerieFondo"
						INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieFondo_Sis" ON ("SerieFondo"."IdSerieFondo" = "SerieFondo_Sis"."IdSerieFondo");
	END;
END;
GO