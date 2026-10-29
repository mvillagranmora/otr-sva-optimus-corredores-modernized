SET SCHEMA "BSCL";



CALL "BSCL"."BSCL.Plataforma.BD.Servicios::BolsaInstrumentoPreciosActual"
(	p_IdEmpresa  => 1 ,
	p_FiltroClasificacion => 'RV' ,
	p_MuestraPrecio => 'N' ,
	p_out_BolsaInstrumentoPrecios => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);

select "FechaVencimiento",* from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "FechaVencimiento" >= '2016-09-05'
