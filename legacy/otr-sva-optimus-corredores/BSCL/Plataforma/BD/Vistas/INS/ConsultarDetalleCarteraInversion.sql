SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarDetalleCarteraInversion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarDetalleCarteraInversion" as
	select 
		ci."NombreCarteraInversion",
		ci."IdEmpresa",
		ci."Descripcion",
		ci."IdEstado",
		dci."IdInstrumento",
		i."Nemotecnico"
	from  "BSCL.Plataforma.BD::INS.CarteraInversion" ci
	inner join "BSCL.Plataforma.BD::INS.DetalleCarteraInversion" dci
		on ci."NombreCarteraInversion" = dci."NombreCarteraInversion"
		and ci."IdEmpresa" = dci."IdEmpresa"
	left join "BSCL.Plataforma.BD::INS.Instrumento" i
		on i."IdInstrumento" = dci."IdInstrumento";
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarDetalleCarteraInversion" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
