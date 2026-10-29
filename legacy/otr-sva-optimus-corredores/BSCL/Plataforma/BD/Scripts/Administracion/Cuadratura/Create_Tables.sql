-- Drop table

-- DROP TABLE BSCL."BSCL.Custodia.BD::CUS.CuadraturaMovimientosCartera";

CREATE TABLE BSCL."BSCL.Plataforma.BD::CUA.SaldoCustodiaRespaldo" (
	"FechaSistema"	NVARCHAR(10),
	"DocumentoIdentidad" NVARCHAR(40),
	"IdPortafolio"	BIGINT,
	"CodigoPortafolio" NVARCHAR(40),
	"IdClasificacionTipoInstrumento" NVARCHAR(3),
	"IdInstrumento"	BIGINT,
	"Nemotecnico" NVARCHAR(40),
	"Saldo" DECIMAL(26,8)
);

DROP TABLE BSCL."BSCL.Plataforma.BD::CUA.MovimientosCustodia";
CREATE TABLE BSCL."BSCL.Plataforma.BD::CUA.MovimientosCustodia" (
	"FechaSistema"	NVARCHAR(10),
	"DocumentoIdentidad" NVARCHAR(40),
	"IdPortafolio"	BIGINT,
	"CodigoPortafolio" NVARCHAR(40),
	"IdClasificacionTipoInstrumento" NVARCHAR(3),
	"IdInstrumento"	BIGINT,
	"Nemotecnico" NVARCHAR(40),
	"Cantidad" DECIMAL(26,8),
	"IdTipoOperacion"	NVARCHAR(6),
	"IdMovimientoComprobante" BIGINT,
	"IdMovimientoCarteraEnTransito" BIGINT,
	"IdCarteraSimultaneaVenta" BIGINT,
	"IdCarteraSimultaneaCompra" BIGINT,
	"IdCarteraPrestaEmpres" BIGINT,
	"IdCarteraOSA"		BIGINT,
	"IdModulo"			NVARCHAR(3)
);

DROP TABLE BSCL."BSCL.Plataforma.BD::CUA.MovimientosCustodiaExclusiones";
CREATE TABLE BSCL."BSCL.Plataforma.BD::CUA.MovimientosCustodiaExclusiones" (
	"IdMovimientoComprobante" BIGINT,
	"IdMovimientoCarteraEnTransito" BIGINT,
	"IdCarteraSimultaneaVenta" BIGINT,
	"IdCarteraSimultaneaCompra" BIGINT,
	"IdCarteraPrestamo" BIGINT,
	"IdCarteraEmprestito" BIGINT,
	"IdCarteraOSA" BIGINT,
	"Comentario" NVARCHAR(5000)
);
rollback


DROP TABLE BSCL."BSCL.Plataforma.BD::CUA.SaldoCustodiaCalculado";
CREATE TABLE BSCL."BSCL.Plataforma.BD::CUA.SaldoCustodiaCalculado" (
	"FechaSistema"	NVARCHAR(10),
	"DocumentoIdentidad" NVARCHAR(40),
	"IdPortafolio"	BIGINT,
	"CodigoPortafolio" NVARCHAR(40),
	"IdClasificacionTipoInstrumento" NVARCHAR(3),
	"IdInstrumento"	BIGINT,
	"Nemotecnico" NVARCHAR(40),
	"Saldo" DECIMAL(26,8),
	"Garantia" DECIMAL(26,8),
	"CompraPlazo" DECIMAL(26,8),
	"VentaPlazo" DECIMAL(26,8)
);

CREATE TABLE BSCL."BSCL.Plataforma.BD::CUA.FechasCuadratura" (
	"Fecha"	NVARCHAR(10)
);



--DELETE FROM BSCL."BSCL.Plataforma.BD::CUA.MovimientosCustodia";
--DELETE FROM BSCL."BSCL.Plataforma.BD::CUA.SaldoCustodiaCalculado";
--DELETE FROM BSCL."BSCL.Plataforma.BD::CUA.MovimientosCustodia";
