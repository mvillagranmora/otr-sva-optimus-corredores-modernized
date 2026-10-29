
SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"IdInstrumento"					BIGINT,		--INSTRUMENTO DE LA GARANTIA
	"IdInstrumentoGarantizado"		BIGINT,		--INSTRUMENTO A GARANTIZAR (SIMULTANEA VENTA)
	"IdCliente"						BIGINT,
	"IdPortafolio"					BIGINT,
	"Cantidad"						DECIMAL(26,8),
	"IdBolsa"						NVARCHAR(3),
	"Precio"						DECIMAL(26,8),
	"Monto"							DECIMAL(26,8)
);

drop TABLE #TT2;
CREATE LOCAL TEMPORARY TABLE #TT2 (
	"IdMoneda"						NVARCHAR(3),
	"IdCliente"						BIGINT,
	"IdPortafolio"					BIGINT,
	"Monto"							DECIMAL(26,8),
	"IdInstrumentoGarantizado"		BIGINT		--INSTRUMENTO A GARANTIZAR (SIMULTANEA VENTA)
);

INSERT INTO #TT
select 502,2248,395174,343710,100,'BCS',100,1000 from dummy;

INSERT INTO #TT2
select 'CLP',395174,343710,1000,2248 from dummy;


CALL "BSCL.Orden.BD.Servicios::CrearGarantiasOrdenes" (
	p_IdEmpresa 				=> 1,
    p_IdUsuario 				=> 1,
    p_IdModulo 					=> 'ORD',
	p_IdMonedaSistema 			=> 'CLP',
    p_FechaSistema 				=> '2016-03-01',
	p_GarantiasCustodia			=> #TT,
	p_GarantiasCtaInv			=> #TT2,
	p_out_IngresosRealizadosCustodia			=> ?,
	p_out_IngresosRealizadosCuentaInversion			=> ?,
	p_out_AdvertenciasContador			=> ?,
	p_out_AdvertenciasDetalle			=> ?,
	p_out_ErroresContador				=> ?,
	p_out_ErroresDetalle				=> ?

);
ROLLBACK
--SELECT "IdTipoReajuste","IdMoneda",* FROM "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'ACN'

--UPDATE  "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" set "IdTipoReajuste" = "IdMoneda" where "IdTipoInstrumento" = 'ACN' and "IdTipoReajuste" = ''
