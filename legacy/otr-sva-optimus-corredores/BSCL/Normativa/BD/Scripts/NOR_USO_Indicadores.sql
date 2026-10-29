SET SCHEMA "BSCL";

/******* INDICADORES DE NORMATIVA *******/
--SERVICIO: BSCL"."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"


--INFORMACION PRIVILEGIADA NCG 69 				--> (s/n)
 SELECT * FROM "BSCL"."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
 WHERE REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='INFPRV' AND REL."ValorIndicador" ='S'


--SUSCEPTIBLE CONFLICTOS DE INTERES				--> (s/n)
 SELECT * FROM "BSCL"."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
 WHERE REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='CONFINT' AND REL."ValorIndicador" ='S'
 

 --RELACION CON LA CORREDORA SEGUN ARTICULO 100 --> (s/n)
 SELECT * FROM "BSCL"."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
 WHERE REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='RELCORR' AND REL."ValorIndicador" ='S'
 
  --RELACION  SEGUN CIRCULAR 1237 				--> (codigo)
 SELECT * FROM "BSCL"."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
 WHERE REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='CI1237' AND REL."CodigoValorIndicador" <>''

 --RELACION  SEGUN NCG 380						--> (codigo)
 SELECT * FROM "BSCL"."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
 WHERE REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='NCG380' AND REL."CodigoValorIndicador" <>''

 --RELACION  SEGUN NCG SVS 69					--> (codigo)
 SELECT * FROM "BSCL"."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
 WHERE REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='NCG69PROP' AND REL."CodigoValorIndicador" <>''

--RELACION  SEGUN CIRCULAR 64					--> (codigo)
 SELECT * FROM "BSCL"."BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
 WHERE REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='1064PROP' AND REL."CodigoValorIndicador" <>''


