SET SCHEMA "BSCL";


CALL "BSCL.Custodia.BD.Servicios::GTA.ConsultarCustodiaParaGarantia" (
	-- Parametros entrada comunes en todos los servicios publicos
	p_IdEmpresa 							=>1,
	p_IdUsuario 							=>1,
	p_IdModulo 							=>'ORD',
	p_IdPais 							=>44,
	p_IdCliente 							=>524896,
	p_IdPortafolio 							=>512357,
	p_IdTipoInstrumento 							=>'SIM',
	p_IdInstrumentoExigeGarantia 							=>2249,
	p_IdBolsa 							=>'BCS',
	p_plazoDias 							=>0,
	p_out_CustodiaGarantia 							=>?,
	p_out_AdvertenciasContador 							=>?,
	p_out_AdvertenciasDetalle 							=>?,
	p_out_ErroresContador 							=>?,
	p_out_ErroresDetalle 							=>?);
	
	
	SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
  "FolioCartera"            BIGINT,
  "FolioGarantia"            BIGINT,
  "FolioSubyacente"          BIGINT,
  "IdCliente"              BIGINT,
  "IdPortafolio"            BIGINT
    
);

drop TABLE #TT2;
CREATE LOCAL TEMPORARY TABLE #TT2    (
  "IdClasificacionTipoInstrumento"  NVARCHAR(3),
  "IdTipoInstrumento"          NVARCHAR(3),
  "IdInstrumento"            BIGINT,
  "IdEmisor"              NVARCHAR(40),
  "FechaVencimiento"          DATE,
  "IdTipoReajuste"          NVARCHAR(3),
  "EsFungible"            NVARCHAR(1),
  "IdBoveda"              NVARCHAR(3)
    );
  
INSERT INTO #TT
SELECT null,null,null, 1222274,1418755
FROM DUMMY;

INSERT INTO #TT2
SELECT null,null,NULL,null,null,null,null,null
FROM DUMMY;

CALL BSCL."BSCL.Custodia.BD.Servicios::ConsultarFlujoCustodia" ( 
          p_IdEmpresa        => 1,
        p_Uso          => 'PARA_GARANTIZAR',
        p_IdPais        => 44,
        p_Instrumentos      => #TT2,
        p_Carteras        => #TT,
        p_FechaSaldo      => '2016-07-05',
          p_out_FlujoDetallado  => ?,
          p_out_FlujoTotalizado  => ?,
          p_DiasProyeccion     => 2,
          p_EsAgrupadoPorFolioCartera => 'N'
      );
