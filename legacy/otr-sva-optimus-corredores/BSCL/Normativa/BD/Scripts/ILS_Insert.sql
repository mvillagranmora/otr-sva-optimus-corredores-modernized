set schema "BSCL";

--delete from "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio" = 13007;
--delete from "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio" = 13008;
--delete from "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio" = 13013;
/*==================================================================*/
/*                BSCL.Plataforma.BD::PLA.Servicio                  */
/*==================================================================*/
 
Insert Into "BSCL.Plataforma.BD::PLA.Servicio"
(
 "IdServicio"
 ,"RutaServicio"
 ,"UsoServicio"
 ,"TipoServicio"
 ,"Descripcion"
)
Values
(13007,'BSCL.Normativa.BD.Servicios::ILS.MantenedorIndice',0,'P','');

Insert Into "BSCL.Plataforma.BD::PLA.Servicio"
(
 "IdServicio"
 ,"RutaServicio"
 ,"UsoServicio"
 ,"TipoServicio"
 ,"Descripcion"
)
Values
(13008,'BSCL.Normativa.BD.Vistas::ILS.ConsultaIndice',0,'Q','');

Insert Into "BSCL.Plataforma.BD::PLA.Servicio"
(
 "IdServicio"
 ,"RutaServicio"
 ,"UsoServicio"
 ,"TipoServicio"
 ,"Descripcion"
)
Values
(13009,'BSCL.Normativa.BD.Vistas::ILS.ConsultaConcepto',0,'Q','');

Insert Into "BSCL.Plataforma.BD::PLA.Servicio"
(
 "IdServicio"
 ,"RutaServicio"
 ,"UsoServicio"
 ,"TipoServicio"
 ,"Descripcion"
)
Values
(13011,'BSCL.Normativa.BD.Vistas::ILS.ConsultaPosicionFraccion',0,'Q','');

Insert Into "BSCL.Plataforma.BD::PLA.Servicio"
(
 "IdServicio"
 ,"RutaServicio"
 ,"UsoServicio"
 ,"TipoServicio"
 ,"Descripcion"
)
Values
(13013,'BSCL.Normativa.BD.Servicios::ILS.MantenedorIndiceConfiguracion',0,'P','');

Insert Into "BSCL.Plataforma.BD::PLA.Servicio"
(
 "IdServicio"
 ,"RutaServicio"
 ,"UsoServicio"
 ,"TipoServicio"
 ,"Descripcion"
)
Values
(13014,'BSCL.Normativa.BD.Vistas::ILS.ConsultaIndiceConfiguracion',0,'Q','');
