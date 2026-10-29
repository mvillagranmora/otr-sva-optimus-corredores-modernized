SELECT F."IdPersona"
	 , F."IdEstado"
	 , F."IdClasificacionFatca"
	 , F."IdClasificacionEmpresaFatca"
	 , F."FechaFirmaFormulario"
	 , C."IdCliente"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" C
	 LEFT JOIN "BSCL.SocioNegocio.BD::PER.Fatca" F ON F."IdPersona" = C."IdPersona"
WHERE C."IdCliente" IN (:idsClientes)
  AND F."IdPersona" IS NOT NULL
  AND F."IdEstado" = 'ACT';