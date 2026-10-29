INSERT INTO "BSCL.Custodia.BD::CUS.LogProcesoPrestamoBlockChain"(
	"IdEmpresa", 
	"FolioCartera", 
	"FechaProceso", 
	"Descripcion", 
	"OrigenProcesoClase", 
	"OrigenProcesoMetodo", 
	"CreadorParametro")
VALUES(
	  :IdEmpresa
	, :FolioCartera
	, :FechaProceso
	, :Descripcion
	, :OrigenProcesoClase
	, :OrigenProcesoMetodo
	, :CreadorParametro);