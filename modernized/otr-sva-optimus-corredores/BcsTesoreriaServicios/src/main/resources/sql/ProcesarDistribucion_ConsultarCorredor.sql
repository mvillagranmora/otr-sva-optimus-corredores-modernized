SELECT "IdPersona","IdCliente","IdPortafolio" FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" 
where "IdEmpresa" = :IdEmpresa AND "EsPortafolioDefecto"='S'