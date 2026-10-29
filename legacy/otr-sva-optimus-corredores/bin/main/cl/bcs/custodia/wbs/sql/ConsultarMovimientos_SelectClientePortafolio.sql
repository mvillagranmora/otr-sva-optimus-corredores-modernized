SELECT "DocumentoIdentidad"
,"CodigoPortafolio"
FROM "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" CCP
WHERE (:DocumentoIdentidad IS NULL OR CCP."DocumentoIdentidad" = :DocumentoIdentidad) 
AND (:CodigoPortafolio IS NULL OR CCP."CodigoPortafolio" = :CodigoPortafolio)