SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarTipoInstrumentoParaGarantia";

CREATE VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarTipoInstrumentoParaGarantia" ( 
	"IdTipoInstrumentoGarantia",
	"IdBolsaTipoInstrumento",
	"IdTipoInstrumentoGarantizado",
	"NombreTipoInstrumentoGarantizado",
	"CodigoTipoInstrumentoGarantizado",
	"IdClasificacionTipoInstrumentoGarantizado",
	"IdEstadoTipoInstrumentoGarantizado",
	"IdBolsa",
	"NombreBolsa",
	"CodigoBolsa",
	"IdEstadoBolsa",
	"ExigeGarantia",
	"IdEntidadDestinoGarantia",
	"IdTipoInstrumentoAval",
	"NombreTipoInstrumentoAval",
	"CodigoTipoInstrumentoAval",
	"IdClasificacionTipoInstrumentoAval",
	"IdEstadoTipoInstrumentoAval",
	"IdEstadoAval",
	"NombreEstadoAval"
	 ) AS select aval."IdTipoInstrumentoGarantia",    
	aval."IdBolsaTipoInstrumento",
    bti."IdTipoInstrumento" as "IdTipoInstrumentoGarantizado",
    tiGarantizado."Nombre" as "NombreTipoInstrumentoGarantizado",
    tiGarantizado."Codigo" as "CodigoTipoInstrumentoGarantizado",
    tiGarantizado."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumentoGarantizado",
    tiGarantizado."IdEstado" as "IdEstadoTipoInstrumentoGarantizado",
    bti."IdBolsa",
    bolsa."Nombre" as "NombreBolsa",
    bolsa."Codigo" as "CodigoBolsa",
    bolsa."IdEstado" as "IdEstadoBolsa",
    bti."ExigeGarantia",
    bti."IdEntidadDestinoGarantia",
    aval."IdTipoInstrumentoPermitido" as "IdTipoInstrumentoAval",
    tiAval."Nombre" as "NombreTipoInstrumentoAval",
    tiAval."Codigo" as "CodigoTipoInstrumentoAval",
    tiAval."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumentoAval",
    tiAval."IdEstado" as "IdEstadoTipoInstrumentoAval",
    aval."IdEstado" as "IdEstadoAval",
    estado."Nombre" as "NombreEstadoAval"

 from "BSCL.Custodia.BD::GTA.TipoInstrumentoParaGarantia" aval
 INNER JOIN "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" bti ON bti."IdBolsaTipoInstrumento" = aval."IdBolsaTipoInstrumento"
 INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tiGarantizado ON tiGarantizado."IdTipoInstrumento" = bti."IdTipoInstrumento"
 INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tiAval ON tiAval."IdTipoInstrumento" = aval."IdTipoInstrumentoPermitido"
 INNER JOIN "BSCL.Plataforma.BD::INS.Bolsa" bolsa ON bolsa."IdBolsa" = bti."IdBolsa"
 INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" estado ON estado."IdEstado" = aval."IdEstado"
 WHERE bti."ExigeGarantia" = 'S'
WITH READ ONLY;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::GTA.ConsultarTipoInstrumentoParaGarantia"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;