SET SCHEMA "BSCL";
DROP TABLE #test_uso;
DROP TABLE #test_busqueda;
DROP TABLE #test_agrupado;

CREATE local temporary table #test_uso(
	"IdTipoOperacion"			 NVARCHAR(6),
	"IdCliente" 	 			 BIGINT,
	"IdPortafolio"				 BIGINT
);

CREATE local temporary table #test_busqueda(
	"IdClasificacionTipoInstrumento" NVARCHAR(3),
	 "IdTipoInstrumento" NVARCHAR(3),
	 "IdInstrumento" BIGINT,
	 "IdEmisor" NVARCHAR(40),
	 "FechaVencimiento" DATE,
	 "IdTipoReajuste" NVARCHAR(3),
	 "EsFungible" NVARCHAR,
	 "IdBoveda" NVARCHAR(3)
);

CREATE local temporary table #test_agrupado(
	 "ClasificacionTipoInstrumento" NVARCHAR(1),
	 "TipoInstrumento" NVARCHAR(1),
	 "Instrumento" NVARCHAR(1),
	 "Emisor" NVARCHAR(1),
	 "Nemotecnico" NVARCHAR(1),
	 "NemoSVS" NVARCHAR(1),
	 "FechaEmisionInstrumento" NVARCHAR(1),
	 "FechaVencimientoInstrumento" NVARCHAR(1),
	 "FechaLiquidacionInstrumento"	NVARCHAR(1),
	 "TipoReajuste" NVARCHAR(1),
	 "EsFungible" NVARCHAR(1),
	 "CodigoIFRS" NVARCHAR(1),
	 "Boveda" NVARCHAR(1),
	 "Bolsa" NVARCHAR(1),
	 "TipoOperacion" NVARCHAR(1),
	 "Mesa" NVARCHAR(1),
	 "FolioRegistro" NVARCHAR(1),
	 "ItemOrigen" NVARCHAR(1)
);

INSERT INTO #test_uso
SELECT 'FLEVCP', null, null FROM DUMMY;

INSERT INTO #test_busqueda
SELECT null,null,null,'LAN',null,null,null,null FROM DUMMY;

INSERT INTO  #test_agrupado
SELECT 'S','S','S','N','N','N','N','N','N','N','N','N','N','N','N','N','N','N' FROM DUMMY;

CALL "BSCL.Custodia.BD.Servicios::ConsultarFlujoCustodia"(
	p_IdEmpresa => 1,
	p_IdUsuario => 21,
	p_IdModulo => 'FMM',
	p_FechaSaldo => '2016-12-05',
	p_IdPais => 44,
	p_DiasProyeccion => 0,
	p_DiaHabil => 'S',
	p_ValorizarFlujo => 'S',
	p_SoloSubyacentes => 'N',
	p_SoloGarantizables => 'N',
	p_Uso => #test_uso,
	p_FiltrosBusqueda => #test_busqueda,
	p_EsAgrupadoPor => #test_agrupado,
	p_out_KeyFlujoCustodia => ?,
	p_out_FlujoTotalizado => ?,
	p_out_FlujoDetallado => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);
