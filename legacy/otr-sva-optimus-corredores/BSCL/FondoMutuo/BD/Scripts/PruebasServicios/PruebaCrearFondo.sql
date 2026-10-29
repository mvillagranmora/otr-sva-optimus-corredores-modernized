set schema bscl;
DROP table #fondos;
create local temporary table #fondos("IdInterno" 				  BIGINT,
		    									"IdFondo"					  NVARCHAR(40),
		    									"IdEmisor"					  NVARCHAR(40),
		    									"Nemotecnico"			  	  NVARCHAR(40),
		    									"NombreFondo"				  NVARCHAR(100),
		    									"IdTipoFondo"				  NVARCHAR(40),
		    									"IdPaisOrigen"				  BIGINT,
		    									"CodigoSVS"				      NVARCHAR(40),
		    									"MoneyMarket"   			  NVARCHAR(1),
		    									"Vigencia"					  NVARCHAR(1),
		    									"FondoExtranjero"			  NVARCHAR(1),
		    									"IdMoneda"					  NVARCHAR(3),
		    									"IdTipoInstrumento"			  NVARCHAR(3),
		    									"IdCategoriaPerfilFondoMutuo" NVARCHAR(40),
		    									"AperturaAutomaticaFondo"	  NVARCHAR(1),
		    									"FondoNormativo"			  NVARCHAR(1) -- SOLO DESARROLLO
);

DELETE FROM  #fondos;
INSERT INTO #fondos 
("IdInterno","IdFondo","IdEmisor","Nemotecnico","NombreFondo","IdTipoFondo","IdPaisOrigen","CodigoSVS","MoneyMarket","Vigencia","FondoExtranjero","IdMoneda","IdTipoInstrumento",
"IdCategoriaPerfilFondoMutuo","AperturaAutomaticaFondo","FondoNormativo" -- SOLO DESARROLLO
)
VALUES(0,'PRUEBA_11','CREDICORP','PRUEBA','NombreFondoA',0,0,'CODSVS','N','N','N','CLP','FFM', '', 'S','N');


call "BSCL.FondoMutuo.BD.Servicios::CrearFondo" (
	p_IdEmpresa	 => 1,
	p_Fondo => #fondos,
	p_out_Resultado => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD::FMM.Fondo";
