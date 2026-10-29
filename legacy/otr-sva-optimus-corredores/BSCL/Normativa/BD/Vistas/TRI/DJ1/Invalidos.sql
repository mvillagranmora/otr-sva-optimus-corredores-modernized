set schema "BSCL";

drop view "BSCL.Normativa.BD.Vistas::TRI.DJ1.Invalidos";

create view "BSCL.Normativa.BD.Vistas::TRI.DJ1.Invalidos"  as
select b."CodigoValidacion", a.*
    from "BSCL.Normativa.BD.Vistas::TRI.DJ1.Datos" a
    inner join "BSCL.Normativa.BD::TRI.ResumenValidacion" b
    on a."IdDocumento"=b."IdDocumento"
    inner join "BSCL.Normativa.BD::TRI.DetalleValidacion" c
    on b."ID"=c."IdResumenValidacion"
    where a."ID"=c."IdDetalleDocumento"
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1.Invalidos" to bcs_desarrollador with grant option;