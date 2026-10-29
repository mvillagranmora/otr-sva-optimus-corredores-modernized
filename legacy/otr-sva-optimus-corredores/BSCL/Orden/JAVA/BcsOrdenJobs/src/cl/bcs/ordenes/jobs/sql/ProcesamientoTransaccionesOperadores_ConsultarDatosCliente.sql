SELECT 
"DocumentoIdentidad",
"CodigoPortafolio",
"IdPortafolio"
FROM "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"
WHERE "DocumentoIdentidad" IN (:DocumentoIdentidad) 
