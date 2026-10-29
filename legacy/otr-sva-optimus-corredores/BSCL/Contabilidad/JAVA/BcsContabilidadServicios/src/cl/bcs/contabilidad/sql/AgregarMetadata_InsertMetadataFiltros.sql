INSERT INTO "BSCL.Contabilidad.BD::ITZ.MetadataFiltros"
(
	"IdMetadataFiltros"
	,"IdMetadataCuentaContable"
	,"CuentaOptimus"
	,"IdTipoDatoEvento"
	,"Condicion"
	,"Valor"
)
VALUES(
	"BSCL.Contabilidad.BD.Secuencias::IdMetadataFiltros".NEXTVAL
	,:IdMetadataCuentaContable
	,:CuentaOptimus
	,:IdTipoDatoEvento
	,:Condicion
	,:Valor
)