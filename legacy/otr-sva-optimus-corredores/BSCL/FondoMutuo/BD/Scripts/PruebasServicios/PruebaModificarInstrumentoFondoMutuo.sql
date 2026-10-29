set schema bscl;
DROP table #seriefondos;
DROP table #fondos;
create local temporary table #seriefondos(
											"IdInterno" 					BIGINT,
			    					  		"IdSerieFondo"					NVARCHAR(40),
			    					  		"IdInstrumento"					BIGINT,
			    					  		"IdFondo"						NVARCHAR(40),
			    					  		"NombreSerieFondo"				NVARCHAR(100),
			    					  		"CodigoSerie"					NVARCHAR(10),
			    					  		"PropositoSerie"				NVARCHAR(30),
			    					  		"Alias"							NVARCHAR(40),
			    					  		"DecimalesValorCuota"		  	BIGINT		,
	 										"DecimalesCuota"			  	BIGINT		,
			    					  		"MontoMinimo"					DECIMAL(28,6),
			    					  		"MontoMaximo"					DECIMAL(28,6),
			    					  		"DiasRetencionesRescate"		BIGINT,
			    					  		"DiasRetencionesAporte"			BIGINT,
			    					  		"DiasRetencionesReinversion"	BIGINT,
			    					  		"ExcluyeGarantia"				NVARCHAR(1),
			    					  		"CobraComision"			  		NVARCHAR(1),
			    					  		"Diferido"					  	NVARCHAR(1),
			    					  		"InstrumentoAGF"			  	NVARCHAR(40),
			    					  		"InstrumentoDCV"			  	NVARCHAR(40),
			    					  		"Omnibus"					  	NVARCHAR(1),
			    					  		"PatrimonioMinimo"				DECIMAL(28,6),
			    					  		"IdSerieFondoOrigen"			NVARCHAR(40),
			    					  		"IdEstado"						NVARCHAR(3),
			    					  		"IdCliente"						BIGINT,
			    					  		"IdPortafolio"					BIGINT,
			    					  		"IdFormaPagoAbono"				BIGINT,
			    					  		"IdFormaPagoCargo"				BIGINT
);



DELETE FROM  #seriefondos;
INSERT INTO #seriefondos VALUES(0,'PRUEBA_2_3',2,'PRUEBA_1','NombreSerieFondo','COD','APV57','Alias',0,0,0,0,0,0,0,'S','N','N','','','N',0,'','ACT',1,1,1,1);

create local temporary table #fondos(
												"IdInterno" 				BIGINT,
		    									"IdFondo"					NVARCHAR(40),
		    									"IdEmisor"					NVARCHAR(40),
		    									"Nemotecnico"				NVARCHAR(40),
		    									"NombreFondo"				NVARCHAR(100),
		    									"IdTipoFondo"				NVARCHAR(40),
		    									"IdRegimenTributario"		NVARCHAR(40),
		    									"IdPaisOrigen"				BIGINT,
		    									"CodigoSVS"					NVARCHAR(40),
		    									"MoneyMarket"   			NVARCHAR(1),
		    									"IdCategoriaInversionista"	NVARCHAR(40),
		    									"Vigencia"					NVARCHAR(1),
		    									"FondoExtranjero"			NVARCHAR(1),
		    									"IdMoneda"					NVARCHAR(3),
		    									"IdTipoInstrumento"			NVARCHAR(3),
		    									"IdFamiliaFondo"			NVARCHAR(40)
);

DELETE FROM  #fondos;
INSERT INTO #fondos VALUES(0,'PRUEBA_1','AGF','PRUEBA','NombreFondoA','1','APV57',0,'CODSVS','N','ARRIESGADO','S','n','CLP','FFM', '');


select * from #fondos;
select * from #seriefondos;



call "BSCL.FondoMutuo.BD.Servicios::ModificarInstrumentoFondoMutuo" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_UsaReglasNegocio => 'S',
	p_Fondo => #fondos,
	p_SerieFondo => #seriefondos,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo";
SELECT * FROM "BSCL.FondoMutuo.BD::FMM.Fondo";
