SET SCHEMA BSCL;

UPSERT "BSCL.Plataforma.BD::PLA.Servicio"
VALUES (69069069, 'BSCL.Normativa.BD.Vistas::TRI.ConsultarTBSICAVC', 0, 'Q', 'Consultar TBSICAVC (Llave compuesta)', '', '', 'S', 'N')
WHERE "IdServicio"=69069069 AND "RutaServicio"='BSCL.Normativa.BD.Vistas::TRI.ConsultarTBSICAVC' AND "TipoServicio"='Q';

COMMIT;