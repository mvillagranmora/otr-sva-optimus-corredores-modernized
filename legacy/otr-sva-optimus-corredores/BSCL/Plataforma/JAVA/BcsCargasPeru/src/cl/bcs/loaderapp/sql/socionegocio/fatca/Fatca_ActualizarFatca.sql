UPDATE F
SET 	
	F."IdClasificacionFatca" = COALESCE(:IdClasificacionFatca, FR."IdClasificacionFatca"),
	F."IdClasificacionEmpresaFatca" = COALESCE(:IdClasificacionEmpresaFatca, FR."IdClasificacionEmpresaFatca"),
	F."NumeroGIIN" = COALESCE(:NumeroGIIN, FR."NumeroGIIN"),
	F."FechaFirmaFormulario" = COALESCE(:FechaFirmaFormulario, FR."FechaFirmaFormulario"),
	F."AutorizacionInformarOrganismo" = COALESCE(:AutorizacionInformarOrganismo, FR."AutorizacionInformarOrganismo"),
	F."TIN1" = COALESCE(:TIN1, FR."TIN1"),
	F."IdPaisNacionalidad2" = COALESCE(:IdPaisNacionalidad2, FR."IdPaisNacionalidad2"),
	F."TIN2" = COALESCE(:TIN2, FR."TIN2"),
	F."IdPaisNacionalidad3" = COALESCE(:IdPaisNacionalidad3, FR."IdPaisNacionalidad3"),
	F."TIN3" = COALESCE(:TIN3, FR."TIN3"),
	F."IdPaisNacionalidad4" = COALESCE(:IdPaisNacionalidad4, FR."IdPaisNacionalidad4"),
	F."TIN4" = COALESCE(:TIN4, FR."TIN4"),
	F."IdPaisNacionalidad5" = COALESCE(:IdPaisNacionalidad5, FR."IdPaisNacionalidad5"),
	F."TIN5" = COALESCE(:TIN5, FR."TIN5"),
	F."ExcepcionResidente" = COALESCE(:ExcepcionResidente, FR."ExcepcionResidente"),
	F."ExcepcionNacimiento" = COALESCE(:ExcepcionNacimiento, FR."ExcepcionNacimiento"),
	F."ExcepcionDomicilio" = COALESCE(:ExcepcionDomicilio, FR."ExcepcionDomicilio"),
	F."ExcepcionTelefono" = COALESCE(:ExcepcionTelefono, FR."ExcepcionTelefono"),
	F."ExcepcionOrdenPermanente" = COALESCE(:ExcepcionOrdenPermanente, FR."ExcepcionOrdenPermanente"),
	F."ExcepcionFirma" = COALESCE(:ExcepcionFirma, FR."ExcepcionFirma"),
	F."ParticipacionSociedadesUSA" = COALESCE(:ParticipacionSociedadesUSA, FR."ParticipacionSociedadesUSA"),
	F."OrdenPermanente" = COALESCE(:OrdenPermanente, FR."OrdenPermanente"),
	
	"FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM "BSCL.SocioNegocio.BD::PER.Fatca" F, "BSCL.SocioNegocio.BD::PER.Fatca" FR	
WHERE F."IdPersona" = :IdPersona AND F."IdPersona" = FR."IdPersona"