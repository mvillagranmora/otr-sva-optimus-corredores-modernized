SELECT 
	FAT."IdClasificacionFatca",
	FAT."IdClasificacionEmpresaFatca",
	FAT."NumeroGIIN" ,
	--FAT."FechaFirmaFormulario" ,
	TO_NVARCHAR(FAT."FechaFirmaFormulario", 'YYYY-MM-DD') AS "FechaFirmaFormulario",
	FAT."AutorizacionInformarOrganismo",
	FAT."NACEEUU",
	FAT."NACEEUUExplicacion", 
	FAT."ObligacionesFiscales",
	FAT."ObligacionesFiscalesExplicacion",
	FAT."ExcepcionResidente",
	FAT."ExcepcionNacimiento",
	FAT."ExcepcionDomicilio",
	FAT."ExcepcionTelefono",
	FAT."ExcepcionOrdenPermanente",
	FAT."ExcepcionFirma",
	FAT."OrdenPermanente"	
FROM "BSCL.SocioNegocio.BD::PER.Fatca" AS FAT
WHERE FAT."IdPersona" IN (:IdPersona);