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
SELECT null,null,null, null,null
FROM DUMMY;

INSERT INTO #TT2
SELECT 'RF',null,261981,null,null,null,'N',null
FROM DUMMY;

CALL "BSCL.Orden.BD.Servicios::ConsultarCarterasAsignacion" (
	p_IdEmpresa 						=> 1,
    p_IdUsuario 						=> 1,
    p_IdModulo 							=> 'ORD',
	p_Instrumentos 						=> #TT2,
    p_Carteras 							=> #TT,
	p_FechaSaldo						=> '2016-07-07',
	p_out_Carteras						=> ?,
	p_out_AdvertenciasContador			=> ?,
	p_out_AdvertenciasDetalle			=> ?,
	p_out_ErroresContador				=> ?,
	p_out_ErroresDetalle				=> ?

);

--SELECT "IdTipoReajuste","IdMoneda",* FROM "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'ACN'

--UPDATE  "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" set "IdTipoReajuste" = "IdMoneda" where "IdTipoInstrumento" = 'ACN' and "IdTipoReajuste" = ''
