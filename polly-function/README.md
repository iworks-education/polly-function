# Polly Function

## Instalação

###AWS Lambda

Configurar as seguintes variáveis de ambiente:

**LANGUAGE_CODE - Valid Codes**

arb | cmn-CN | cy-GB | da-DK | de-DE | en-AU | en-GB | en-GB-WLS | en-IN | en-US | es-ES | es-MX | es-US | fr-CA | fr-FR | is-IS | it-IT | ja-JP | hi-IN | ko-KR | nb-NO | nl-NL | pl-PL | pt-BR | pt-PT | ro-RO | ru-RU | sv-SE | tr-TR

**VOICE_ID - Valid Codes**

Para PT-BR: 

	Vitória, Female
	Camila, Female
	Ricardo, Male
	
Para EN-US

	Salli, Female
	Joanna, Female
	Ivy, Female
	Kendra, Female
	Kimberly, Female
	Matthew, Male
	Justin, Male
	Joey, Male
	
**OUTPUT_ MP3 _BUCKET**

	Nome do Bucket que a Lambda irá depositar o arquivo MP3 gerado pela Amazon Polly
	
**TOPIC_ARN**
	
	ARN do Tópico
	
###Passo a Passo

1. Criar Amazon S3 Bucket de Entrada
2. Criar Amazon S3 Bucket de Saída para os áudios
3. Criar o tópico SNS e inserir uma subscrição de E-mail
4. Criar a AWS Lambda e configurar as varíaveis de Ambiente
5. Configurar trigger com o Amazon S3 Bucket de Entrada
6. Subir Arquivo XML utilizando escrito com Speech Synthesis Markup Language (SSML)

### SSML (Speech Synthesis Markup Language)

	https://docs.aws.amazon.com/polly/latest/dg/supportedtags.html
	
