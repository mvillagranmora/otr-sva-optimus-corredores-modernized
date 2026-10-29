SELECT VDE."IdPersona", VDE."IdDireccionElectronica", VDE."Direccion" 
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" VDE
WHERE VDE."Direccion" IN (UPPER(:correosDestinatarios)) OR VDE."Direccion" IN (LOWER(:correosDestinatarios))