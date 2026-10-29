SET SCHEMA "BSCL"; 


DROP TABLE "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_SRV_TablaCodigosMensajes"  CASCADE;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_SRV_TablaCodigosMensajes"
(
	"IdInterno" 	BIGINT,
	"Codigo" 		NVARCHAR(100),
	"CodigoDato1" 	NVARCHAR(100),
	"ValorDato1" 	NVARCHAR(520),
	"CodigoDato2" 	NVARCHAR(100),
	"ValorDato2" 	NVARCHAR(520),
	"Resultado"		NVARCHAR(4)
)  ON COMMIT DELETE ROWS;


DROP TABLE "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_OUT_InformacionDetalle"  CASCADE;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_OUT_InformacionDetalle"
(
	"IdInterno" 	BIGINT,
	"Codigo" 		NVARCHAR(100),
	"Descripcion"	NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;

DROP TABLE "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_OUT_ErroresDetalle"  CASCADE;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Plataforma.BD.Servicios::PLA.CrearConceptoManual_OUT_ErroresDetalle"
(
	"IdInterno" 	BIGINT,
	"Codigo" 		NVARCHAR(100),
	"Descripcion"	NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;

--------------------------------------------------------------------------------------------------------------------------------------

DROP TABLE "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_SRV_TablaCodigosMensajes"  CASCADE;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_SRV_TablaCodigosMensajes"
(
	"IdInterno" 	BIGINT,
	"Codigo" 		NVARCHAR(100),
	"CodigoDato1" 	NVARCHAR(100),
	"ValorDato1" 	NVARCHAR(520),
	"CodigoDato2" 	NVARCHAR(100),
	"ValorDato2" 	NVARCHAR(520),
	"Resultado"		NVARCHAR(4)
)  ON COMMIT DELETE ROWS;

DROP TABLE "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_OUT_InformacionDetalle"  CASCADE;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_OUT_InformacionDetalle"
(
	"IdInterno" 	BIGINT,
	"Codigo" 		NVARCHAR(100),
	"Descripcion"	NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;

DROP TABLE "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_OUT_ErroresDetalle"  CASCADE;
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Plataforma.BD.Servicios::PLA.EditarConceptoManual_OUT_ErroresDetalle"
(
	"IdInterno" 	BIGINT,
	"Codigo" 		NVARCHAR(100),
	"Descripcion"	NVARCHAR(5000)
)  ON COMMIT DELETE ROWS;


COMMIT;