select "IdDocumento","IdUltimoAnexo"
from "BSCL.Plataforma.BD::DOC.Documento"
WHERE "IdTipoDocumento" = :p_IdTipoDocumento AND "IdCliente" = :p_IdCliente