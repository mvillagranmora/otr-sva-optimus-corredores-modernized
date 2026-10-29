SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Invalidos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Invalidos" (
    "CodigoValidacion",
	"ID" ,
	"IdDocumento" ,
	"VboUsuario" ,
	"Observaciones" ,
	"FuenteDatos" ,
	"Numero" ,											-- 	Sec_Movto	IdSolicitudAporteRescate
	"FolioCartera" ,							--	Sec_Car_Inver
	"IdFondo" ,
	"Nemo" ,
	"IdEmisor" ,
	"RutEmisor" ,									--C1 RutAdministradora
	"RutParticipe"  ,								--C2
	"RutFondo"  ,									--C3
	"RunFondo"  ,									--C3
	"TipoOperacion" ,								--C5
	"FechaOperacion" ,									
	"FechaInversion" ,									--C6
	"MontoNominalInversionActualizado" ,		--C7
	"MontoHistorico" ,
	"FechaRescate" ,										--C8
	"MontoNominalRescate" ,					--C9
	"MayorValorActualizado" ,					--C10
	"MenorValorActualizado" ,					--C12
	"RutAdminitradoraFondoReceptor" ,				--C13
	"MontoReinvertido" ,						--C14
	"NumeroCertificado" ,								--C16
	"Cantidad" ,
	"CostoUf" ,
	"IdCliente",  
	"IdPortafolio",
	"TipoDeclarante"
) AS
SELECT
    b."CodigoValidacion",
	a."ID" ,
	a."IdDocumento" ,
	a."VboUsuario" ,
	a."Observaciones" ,
	a."FuenteDatos" ,
	a."Numero" ,											-- 	Sec_Movto	IdSolicitudAporteRescate
	a."FolioCartera" ,							--	Sec_Car_Inver
	a."IdFondo" ,
	a."Nemo" ,
	a."IdEmisor" ,
	a."RutEmisor" ,									--C1 RutAdministradora
	a."RutParticipe"  ,								--C2
	a."RutFondo"  ,									--C3
	a."RunFondo"  ,									--C3
	a."TipoOperacion" ,								--C5
	a."FechaOperacion" ,									
	a."FechaInversion" ,									--C6
	a."MontoNominalInversionActualizado" ,		--C7
	a."MontoHistorico" ,
	a."FechaRescate" ,										--C8
	a."MontoNominalRescate" ,					--C9
	a."MayorValorActualizado" ,					--C10
	a."MenorValorActualizado" ,					--C12
	a."RutAdminitradoraFondoReceptor" ,				--C13
	a."MontoReinvertido" ,						--C14
	a."NumeroCertificado" ,								--C16
	a."Cantidad" ,
	a."CostoUf" ,
	a."IdCliente",  
	a."IdPortafolio",
	a."TipoDeclarante"
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Datos" a
INNER JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" b ON (b."IdDocumento" = a."IdDocumento")
INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" c ON (c."IdResumenValidacion" = b."ID")
WHERE a."ID" = c."IdDetalleDocumento"
with read only;
    
grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1894.Invalidos" to bcs_desarrollador with grant option;
