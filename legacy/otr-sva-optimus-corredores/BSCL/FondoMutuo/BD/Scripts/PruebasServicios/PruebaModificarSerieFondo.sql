set schema bscl;
DROP table #seriefondos;
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
INSERT INTO #seriefondos VALUES(0,'PRUEBA_2_2',2,'PRUEBA_1','NombreSerieFondoA','COD','APV57','Alias',0,0,0,0,0,0,0,'S','N','N','','','N',0,'','ACT',1,1,1,1);


select * from #seriefondos;



call "BSCL.FondoMutuo.BD.Servicios::ModificarSerieDeFondo" (
	p_IdEmpresa	 => 1,
	p_SerieFondo => #seriefondos,
	p_out_Resultado => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SerieDeFondo";
