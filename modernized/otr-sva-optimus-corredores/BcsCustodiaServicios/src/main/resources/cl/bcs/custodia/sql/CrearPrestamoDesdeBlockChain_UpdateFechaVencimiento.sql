update "BSCL.Custodia.BD::CUS.Cartera"
set 
	"FechaVencimientoOperacion" = :FechaVencimiento
where 
	"FolioCartera" = :FolioCartera