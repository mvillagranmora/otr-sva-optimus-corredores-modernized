SELECT "IdIndicador",
"IdTipoIndicador",
"IdEntidad",
"ValorIndicador" FROM "BSCL.Plataforma.BD::IND.Indicador" 
WHERE "IdTipoEntidad" = 'PORTAFOLIO' AND "ModeloCliente" = (:ModeloCliente)