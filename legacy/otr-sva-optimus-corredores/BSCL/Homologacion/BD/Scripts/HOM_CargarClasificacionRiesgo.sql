

---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
---------------------------------------- Actualiza Clasificacion de Riesgo ------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
/*
	SELECT "IdTipoInstrumento","Nemotecnico", "IdEntidadClasificadoraRiesgo", "ValorClasificacionRiesgo", "IdClasificacionRiesgo" 
	from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"  where "Nemotecnico" in ('AGUAS-A','AGUAS-B','BANMEDICA','EST5131011','ALICORC1','BHITS-A','BHITS-B','BHQIT-A1',
	'LAN','COPEC','ENDESA','CAP','ABNA050101','ABNA151100','ABNA200101');
	select * from "BSCL"."BSCL.Plataforma.BD::INS.ClasificacionRiesgo" 
*/
		
	DROP TABLE #Entrada;
	CREATE LOCAL TEMPORARY TABLE #Entrada ( "IdEntidadClasificadoraRiesgo"   NVARCHAR(10), 
 										"IdClasificacionTipoInstrumento" NVARCHAR(10),
 										"Nemotecnico" 					 NVARCHAR(40), 
 										"ValorClasificacionRiesgo" 		 NVARCHAR(20)
									  );

	--- clasifica todo RF contra la BCS
	call "BSCL"."BSCL.Mercado.BD.Servicios::ActualizaClasificacionDeRiesgo"(1, '2016-04-21', 'BCS', #Entrada,?,?);							  
	

	DROP TABLE #Entrada;
	CREATE LOCAL TEMPORARY TABLE #Entrada ( "IdEntidadClasificadoraRiesgo"   NVARCHAR(10), 
 										"IdClasificacionTipoInstrumento" NVARCHAR(10),
 										"Nemotecnico" 					 NVARCHAR(40), 
 										"ValorClasificacionRiesgo" 		 NVARCHAR(20)
									  );
									 		
	--- RV CON OTRA ENTIDAD
	insert into #Entrada values ('FELLER','RV','LAN'  ,'NIVEL 1');
	insert into #Entrada values ('FELLER','RV','COPEC','NIVEL 2');
	insert into #Entrada values ('STDANDPOOR','RV','ENDESA','A');
	insert into #Entrada values ('STDANDPOOR','RV','CAP','1'); -- no tenemos esta clasificacion creada, solo N1, debe dejar 0 en IdClasificacion de la tabla

	--- Respeta la clasificacion de la entidad que viene en tabla
	call "BSCL"."BSCL.Mercado.BD.Servicios::ActualizaClasificacionDeRiesgo"(1, '2016-04-21', '', #Entrada,?,?);


	DROP TABLE #Entrada;
	CREATE LOCAL TEMPORARY TABLE #Entrada ( "IdEntidadClasificadoraRiesgo"   NVARCHAR(10), 
 										"IdClasificacionTipoInstrumento" NVARCHAR(10),
 										"Nemotecnico" 					 NVARCHAR(40), 
 										"ValorClasificacionRiesgo" 		 NVARCHAR(20)
									  );
--- RF Clasificaciones las toma de la BOLSA, pero yo lo doy los nemos y la entidad de referencia. Si no le doy la entidad, toma la por defecto
	insert into #Entrada values ('FELLER','RF','ABNA050101'  ,'');
	insert into #Entrada values ('','RF','ABNA151100','');
	insert into #Entrada values ('','RF','ABNA200101',''); -- no tenemos esta clasificacion creada, solo N1, debe dejar 0 en IdClasificacion de la tabla

	call "BSCL"."BSCL.Mercado.BD.Servicios::ActualizaClasificacionDeRiesgo"(1, '2016-04-21', 'BCS', #Entrada,?,?);
