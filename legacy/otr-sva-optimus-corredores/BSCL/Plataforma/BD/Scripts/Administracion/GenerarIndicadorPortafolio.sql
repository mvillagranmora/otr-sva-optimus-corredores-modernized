--select * from "BSCL"."BSCL.Plataforma.BD::IND.IndicadorPortafolio"
--select count(1) from "BSCL"."BSCL.Plataforma.BD::IND.IndicadorPortafolio"
--select distinct count(1) from "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"
set schema bscl;

insert into "BSCL"."BSCL.Plataforma.BD::IND.IndicadorPortafolio"
("IdCliente","IdPersona","IdTipoCliente",
                  "IdPortafolio","IdTipoEntidad","NombreTipoIndicador","GrupoIndicador",
                  "IdUso","IdTipoIndicador","CodigoTipoIndicador","ClaseDato",
                  "CantidadDecimales","LongitudMinima","LongitudMaxima","IdTipoPersona",
                  "ModeloCliente","ValorIndicador","CodigoValorIndicador","NombreValorIndicador","IdIndicador")
Select  
        		  "IdCliente",
                  "IdPersona",
                  "IdTipoCliente",
                  "IdPortafolio",
                  "IdTipoEntidad",
                  "NombreTipoIndicador",
                  "GrupoIndicador",
                  "IdUso",
                  "IdTipoIndicador",
                  "CodigoTipoIndicador",
                  "ClaseDato",
                  "CantidadDecimales",
                  "LongitudMinima",
                  "LongitudMaxima",
                  "IdTipoPersona",
                  "ModeloCliente",
                  "ValorIndicador",
                  "CodigoValorIndicador",
                  "NombreValorIndicador",
                  "IdIndicador"

        from "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolioAnalitica"
        where "IdTipoIndicador" >= 20 and "IdTipoIndicador" <50
		
commit;

delete from "BSCL"."BSCL.Plataforma.BD::IND.IndicadorPortafolio"

SELECT "IdTipoIndicador",*
from "BSCL.Plataforma.BD::IND.TipoIndicador" where "IdTipoEntidad" = 'PORTAFOLIO'

group by "IdTipoIndicador"








