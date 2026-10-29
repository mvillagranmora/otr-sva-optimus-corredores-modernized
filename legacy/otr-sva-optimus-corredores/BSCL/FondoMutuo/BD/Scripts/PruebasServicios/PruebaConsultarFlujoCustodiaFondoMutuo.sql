SET SCHEMA BSCL;

DROP TABLE #TEMP_TEST_CARTERAS;
DROP TABLE #TEMP_TEST_INSTRUMENTOS;

CREATE local temporary table #TEMP_TEST_INSTRUMENTOS(
"IdInstrumento"	BIGINT
);

CREATE local temporary table #TEMP_TEST_CARTERAS(
"IdCliente"		BIGINT,
"IdPortafolio"	BIGINT
);

INSERT INTO #TEMP_TEST_INSTRUMENTOS
SELECT 351321 FROM DUMMY;

INSERT INTO #TEMP_TEST_CARTERAS
SELECT 524891, 512348 FROM DUMMY;

CALL "BSCL.FondoMutuo.BD.Servicios::ConsultarFlujoCustodiaFondoMutuo"(
	p_IdEmpresa => 1,
    p_IdUsuario	=> 21,
    p_IdModulo	=> 'FMM',
	p_IdPais => 44,
	p_Instrumentos => #TEMP_TEST_INSTRUMENTOS,
	p_Carteras	=> #TEMP_TEST_CARTERAS,
	p_FechaSaldo => '2016-12-06',--Fecha a la que se consulta el saldo
	p_DiasProyeccion => 0,     --cantidad de dias a la q se proyecta el saldo consultado
	
    --VARIABLES DE SALIDA OBLIGATORIAS
    p_out_FlujoFondoMutuoTotalizado	=> ?
);

