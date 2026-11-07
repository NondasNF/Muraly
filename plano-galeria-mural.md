# Galeria Mural

## Plano de desenvolvimento — galeria de recordações em calendário para Android

**Versão:** 0.1 · 07/11/2026  
**Status:** proposta para validação  
**Plataforma:** Android nativo (Kotlin + Jetpack Compose)  
**Conceito visual:** [`docs/mockup-calendario.png`](docs/mockup-calendario.png)

\---

## 1\. Em uma página

A **Galeria Mural** transforma a galeria do celular em um calendário afetivo: cada dia é um quadradinho do mural e a foto escolhida pelo usuário vira o fundo daquele dia. O número do dia continua visível, pequeno, no canto inferior direito.

O fluxo central tem três passos:

1. **Abrir o app** e ver o mês atual como um mural de fotos.
2. **Tocar em um dia** para ver todas as fotos tiradas naquela data.
3. **Escolher uma foto como capa** — ela passa a preencher o quadradinho do dia no calendário.

### Decisões recomendadas

|Tema|Recomendação|Motivo|
|-|-|-|
|Tecnologia|Kotlin + Jetpack Compose, app nativo|Acesso direto ao MediaStore, permissões e performance de galeria; sem ponte de framework|
|Android mínimo|minSdk 26 (Android 8.0)|`java.time` nativo, cobre a grande maioria dos aparelhos ativos|
|Fotos|Ler do `MediaStore`, sem copiar arquivos|Zero duplicação, a galeria continua sendo a fonte da verdade|
|Capas|Banco local (Room) só com metadados|Persistente, leve e privado; a imagem continua no lugar|
|Nuvem|Fora do MVP|Reduz escopo, custo e riscos de privacidade|
|Permissões|Acesso total às fotos, com tratamento de acesso parcial (Android 14+)|O calendário precisa enxergar a galeria inteira para funcionar|
|Prazo|13–16 dias úteis (1 dev em tempo integral)|Ver cronograma na seção 11|

**Premissa importante:** este plano assume que as fotos já existem na **galeria do aparelho** e são lidas via MediaStore. Se a ideia for um álbum interno (o usuário importa fotos para dentro do app), a camada de dados e o onboarding mudam — ver decisão nº 11.

### O que o MVP não é

* Não é editor de fotos (cortes, filtros, colagens).
* Não é backup ou sincronização em nuvem.
* Não tem feed social, compartilhamento ou comentários.
* Não suporta vídeos, GIFs ou fotos ao vivo como capa.
* Não tem múltiplas capas por dia nem álbuns manuais.
* Não importa fotos para dentro do app: ele lê a galeria do sistema.
* Não exige login, conta ou internet.

\---

## 2\. Conceito e princípios de design

**A foto é a protagonista; o calendário é a estrutura.**

1. **Mural, não grade infinita.** O mês é a unidade de leitura. A pessoa reconhece a viagem, a festa, a semana difícil — não uma sequência de miniaturas iguais.
2. **O número do dia recua, mas nunca desaparece.** Ele é a âncora funcional do bloco: 10 sp, branco, no canto inferior direito, sobre um degradê sutil que garante contraste mesmo em fotos claras.
3. **Dois toques para uma lembrança.** Abrir o dia → tocar na foto → definir. Sem menus escondidos no caminho principal.
4. **Silêncio visual.** Fora das fotos, a interface é papel: tipografia serifada nos títulos, cor terracota como acento, cantos arredondados e sombras quase imperceptíveis.
5. **Privacidade radical.** Nenhuma foto sai do aparelho. O app não precisa de internet para funcionar — e não deve pedir essa permissão no MVP.

### Direção visual proposta

|Token|Claro|Escuro|
|-|-|-|
|Fundo|`#FAF5ED` (papel)|`#17130F`|
|Texto|`#2A211A`|`#F3EAE0`|
|Texto secundário|`#8B7A67`|`#B9A896`|
|Acento (terracota)|`#B4451F`|`#E07A4F`|
|Célula vazia|`#F0E7D8`|`#24201C`|
|Célula futura|`#F4EDE2`|`#1E1A16`|

Tipografia: **Georgia** (títulos e data por extenso) + **Segoe UI / system sans** (números, corpo e ações). No Android, a serifada pode ser a `serif` do sistema ou uma fonte empacotada (ex.: Lora, Source Serif) — decidir no M0.

\---

## 3\. Referência visual

!\[Conceito de interface — calendário e dia](docs/mockup-calendario.png)

O mockup acima é um estudo de interface, não uma tela final. As fotos são placeholders gerados para o conceito, e o "hoje" é fictício (12/11) para mostrar um mês com mais capas.

O que ele fixa como direção:

1. **Capa preenchendo o dia** com `ContentScale.Crop`, cantos de 10–12 dp e degradê inferior.
2. **Número reduzido** no canto inferior direito, com sombra leve.
3. **Hoje** com anel terracota de 2 dp e um ponto no canto superior esquerdo.
4. **Dias sem capa** em papel texturizado, com o número em tom quente.
5. **Cartão "Hoje"** abaixo do calendário, como atalho afetivo para o dia atual (recomendado, mas opcional no MVP).
6. **Tela do dia** com a capa atual identificada e uma barra de ação clara para definir a nova capa.

\---

## 4\. Escopo funcional do MVP

### 4.1 Histórias

|ID|História|Prioridade|
|-|-|-|
|US-01|Ver o mês atual em um calendário com as capas dos dias|Essencial|
|US-02|Navegar para meses anteriores por gesto e por setas|Essencial|
|US-03|Abrir um dia e ver apenas as fotos daquela data|Essencial|
|US-04|Definir uma foto como capa do dia|Essencial|
|US-05|Trocar ou remover a capa de um dia|Essencial|
|US-06|Manter as capas após fechar e reabrir o app|Essencial|
|US-07|Entender e conceder o acesso às fotos (inclusive acesso parcial)|Essencial|
|US-08|Ver estados vazios e de erro sem travar|Essencial|
|US-09|Ver o dia de hoje destacado|Importante|
|US-10|Desfazer a definição de capa por engano|Importante|
|US-11|Cartão "Hoje" com a capa do dia|Desejável|

### 4.2 Critérios de aceite

**US-01 / US-02 — Calendário e navegação**

* Ao abrir, o app posiciona no mês atual, com o dia de hoje destacado.
* O título mostra mês e ano por extenso em pt-BR ("Novembro 2026").
* Deslizar horizontalmente e tocar nas setas troca de mês com a mesma posição de scroll.
* A seta de avançar fica desabilitada no mês atual; não é possível navegar para o futuro.
* Meses sem nenhuma foto continuam navegáveis e mostram a grade vazia.
* A grade mantém a mesma altura entre meses (sem "pulo" visual ao trocar de página).

**US-03 — Dia**

* Tocar em qualquer dia abre a tela do dia, inclusive dias sem fotos.
* O título mostra a data por extenso ("12 de novembro") e o subtítulo informa o total de fotos.
* A grade lista todas as fotos cujo horário local cai naquele dia, mais recentes primeiro.
* Dias sem fotos mostram um estado vazio com ação para voltar ao calendário.

**US-04 / US-05 — Capa**

* Tocar em uma foto a seleciona (anel visível) e habilita "Definir como capa".
* Ao confirmar, a capa é gravada e o calendário reflete a mudança ao voltar, sem recarregar a tela.
* A capa atual é identificada na grade do dia com o selo "CAPA".
* "Remover capa" restaura a célula vazia e pode ser desfeito.
* Definir uma nova capa substitui a anterior no mesmo dia (uma capa por dia).
* A operação é atômica: ou grava a nova capa, ou mantém a anterior.

**US-06 / US-10 — Persistência e desfazer**

* Fechar e reabrir o app mantém todas as capas.
* A snackbar "Capa definida" oferece "Desfazer" por pelo menos 5 segundos.
* O "Desfazer" restaura exatamente a capa anterior (ou a ausência dela).

**US-07 — Permissões**

* O app explica o motivo antes de abrir o diálogo do sistema.
* Negar o acesso não quebra o app: o calendário aparece, com um estado explicativo e um botão para tentar de novo.
* Acesso parcial (Android 14+) é detectado e comunicado com um banner e um caminho para ampliar a seleção.
* O app nunca solicita `MANAGE\_EXTERNAL\_STORAGE`.

**US-08 / US-09 — Estados e hoje**

* Sem fotos no aparelho: estado vazio com orientação, sem erro.
* Foto de capa removida da galeria: a célula mostra um placeholder elegante; a capa não é apagada automaticamente; o dia oferece reescolher.
* Hoje é destacado de forma consistente em claro e escuro.
* Rotação de tela e fonte em 200% não escondem o número do dia nem quebram a grade.

**US-11 — Cartão "Hoje" (opcional)**

* Mostra a capa do dia atual, a data e a contagem de fotos.
* Tocar abre o dia; se não houver capa, mostra a foto mais recente ou um estado convidativo.

### 4.3 Fora do MVP, mas no radar

* Exportar o calendário como imagem/PDF para compartilhar.
* Backup/restauração das capas (arquivo `.json` + miniaturas).
* Widget de tela inicial (Glance) com a capa de hoje.
* Notificação "Neste dia, há 1 ano…".
* Legendas e notas por dia.
* Múltiplas capas por dia (colagem).
* Busca por pessoa, lugar ou período.
* Suporte a tablets e dobráveis com layout de dois painéis.
* Sincronização opcional (Google Drive) — só depois de uma conversa explícita sobre privacidade.

\---

## 5\. Telas e fluxos

### 5.1 Calendário (tela inicial)

**Anatomia, de cima para baixo:**

|Área|Conteúdo|Observações|
|-|-|-|
|Barra de status|Relógio e ícones do sistema|Edge-to-edge, ícones escuros no tema claro|
|Cabeçalho|"Novembro" (serifada, 27 sp) + "2026" + setas|Setas em botões circulares de 32 dp|
|Dias da semana|D S T Q Q S S|Início no domingo por padrão (pt-BR)|
|Grade|7 colunas × até 6 linhas|4 dp de espaço entre células|
|Destaque|Cartão "Hoje" (opcional)|Proporção 7:6 ou 4:3, com degradê e texto|
|Rodapé|Dica de gesto / estatística do mês|Discreto, 10 sp|

**Comportamento:**

* `HorizontalPager` com uma página por mês. O gesto horizontal troca de mês; as setas fazem o mesmo.
* O pager inicia no mês atual e só permite páginas até ele.
* Ao voltar do dia, o mês e a posição permanecem.
* Toque simples abre o dia. Toque longo (M4, opcional) abre um seletor rápido de capa.
* Um leve feedback tátil (`HapticFeedbackType.LongPress` ou `TextHandleMove`) acompanha a definição da capa, não a navegação.

**Estados:**

* **Carregando:** esqueleto da grade com células em papel; sem spinner central.
* **Sem permissão:** grade vazia + cartão explicativo no lugar do "Hoje".
* **Acesso parcial:** banner fino acima da grade ("Você compartilhou algumas fotos · Gerenciar").
* **Sem fotos:** ilustração leve + "Quando você tirar fotos, elas aparecem aqui".
* **Erro de consulta:** mensagem curta + "Tentar novamente".
* **Foto de capa ausente:** célula com fundo papel e um ícone discreto de imagem quebrada; o número continua.

### 5.2 Dia

**Anatomia:**

* Barra superior: voltar (36 dp), título e menu de contexto (⋯).
* Cabeçalho: "12 de novembro" (serifada, 27 sp), "quinta-feira · 6 fotos · toque para escolher a capa".
* Grade de fotos: 2 colunas em dias com até 9 fotos; 3 colunas acima disso. Espaço de 6 dp.
* Capa atual: borda terracota de 2 dp + selo "CAPA".
* Foto selecionada: anel claro + ícone de check.
* Barra inferior: miniatura da seleção, texto "Definir como capa do dia" e botão "Definir".
* Menu ⋯: "Remover capa", "Abrir na galeria do sistema" (opcional).

**Interações:**

* Toque seleciona a foto; toque na foto já selecionada abre o visualizador em tela cheia (M4).
* Toque longo abre o visualizador (M4).
* "Definir" grava, volta ao calendário e mostra snackbar com "Desfazer".
* Se o dia não tiver capa, a barra mostra "Definir como capa" e nenhuma foto tem selo.
* Se a capa atual tiver sido apagada da galeria, o app mostra "Capa indisponível" e permite escolher outra.

### 5.3 Navegação

```
Calendário ──toque no dia──▶ Dia ──toque longo──▶ Visualizador (M4)
     ▲                         │
     └────────voltar───────────┘
```

* Navigation Compose com rotas tipadas (`CalendarRoute`, `DayRoute(dateKey)`).
* `dateKey` no formato ISO `YYYY-MM-DD` (ex.: `2026-11-12`).
* O botão voltar do sistema retorna ao calendário preservando o estado.
* Sem deep links no MVP.

\---

## 6\. Arquitetura técnica

### 6.1 Stack

|Camada|Escolha|Por quê|
|-|-|-|
|Linguagem|Kotlin 2.x|Padrão Android moderno, corrotinas, null-safety|
|UI|Jetpack Compose + Material 3|Grid customizado, animações e estados declarativos|
|Navegação|Navigation Compose (rotas tipadas)|Simples e suficiente para 2–3 telas|
|Injeção de dependência|Hilt|Padrão, testável, sem boilerplate manual|
|Persistência|Room + KSP|Flow reativo para as capas, migrações versionadas|
|Imagens|Coil 3 (`AsyncImage`)|Suporte nativo a `content://`, cache de memória e disco, decodificação no tamanho da célula|
|Preferências|DataStore (Proto ou Preferences)|Início da semana, tema, futuras opções|
|Datas|`java.time` (`LocalDate`, `YearMonth`)|API 26+, sem desugaring|
|Assíncrono|Coroutines + Flow|Consultas em `Dispatchers.IO`, estado reativo|
|Testes|JUnit4, Truth, Turbine, Robolectric, Compose UI Test, Roborazzi|Cobre lógica de datas, ViewModel, UI e regressão visual|
|Build|Gradle 8.x + version catalog|Versões centralizadas; fixar as estáveis no M0|
|SDK|`compileSdk 36`, `targetSdk 36`, `minSdk 26`|Verificar a exigência vigente da Play na época do release|

> As versões exatas (Compose BOM, AGP, Room, Hilt, Coil) devem ser fixadas no M0, com as estáveis do momento. O plano evita números que envelhecem rápido.

### 6.2 Por que nativo

* **MediaStore** é a API central do produto. Em Flutter, o acesso passa por plugins que costumam ter lacunas justamente em permissões parciais do Android 14+ e em metadados como `DATE\_TAKEN`.
* **Performance de grade**: decodificação de miniaturas, cache e prefetch são mais previsíveis com Coil + Compose.
* **Tamanho e inicialização**: um app de galeria deve abrir rápido e ocupar pouco; sem runtime extra.
* **Permissões**: o fluxo de `READ\_MEDIA\_VISUAL\_USER\_SELECTED` é sensível e muda entre versões; controlar o código nativo reduz surpresas.

Flutter/KMP só se fizer sentido quando houver um plano concreto de iOS. Mesmo assim, a camada de dados continuaria específica por plataforma.

### 6.3 Camadas e organização

```
app/
└── src/main/java/br/com/mural/
    ├── MuralApp.kt
    ├── MainActivity.kt
    ├── navigation/
    │   ├── MuralNavHost.kt
    │   └── Routes.kt
    ├── core/
    │   ├── date/            # dateKey, month math, week start
    │   ├── permissions/     # PhotoAccess, requests, partial access
    │   └── ui/              # tema, componentes, formatadores
    ├── data/
    │   ├── photos/          # MediaStorePhotoSource, PhotoRepository
    │   └── covers/          # DayCoverEntity, Dao, CoverRepository, Database
    └── feature/
        ├── calendar/        # CalendarScreen, CalendarViewModel, DayCell
        ├── day/             # DayScreen, DayViewModel, PhotoGrid
        └── viewer/          # (M4) visualizador em tela cheia
```

Um único módulo no MVP. A separação por feature evita que o projeto vire um monólito de pacotes; se o tempo de build crescer, extrair `:core` e `:data` depois.

**Fluxo de dados:**

```
MediaStore ──query por mês──▶ PhotoRepository ──contagens──┐
                                                          ├──▶ CalendarViewModel ──StateFlow──▶ Compose
Room ──Flow<List<DayCover>>──▶ CoverRepository ──map───┘

Room ◀──upsert/delete── DayViewModel ◀──seleção do usuário
```

* `CalendarViewModel` combina `covers` (Flow) + `monthPhotos` (consulta sob demanda) em `CalendarUiState`.
* `DayViewModel` recebe `dateKey` via `SavedStateHandle`, consulta as fotos do dia e observa a capa.
* A gravação da capa é feita em `CoverRepository.setCover(...)`, com `@Upsert` e validação de existência da foto.
* Eventos de UI (snackbar, erro) via `Channel<UiEvent>` ou `SharedFlow`, nunca dentro do estado persistente.

### 6.4 Dependências que valem atenção

* **Coil**: configurar `ImageLoader` com cache de memória em 25% da memória disponível e cache de disco em 100–250 MB. Para células, usar `ImageRequest.size(cellPx)` e `precision(INEXACT)` para não decodificar a foto inteira.
* **Room**: exportar o schema (`schemas/`) e escrever migrações desde a versão 1; a tabela de capas é pequena, mas o hábito evita perda de dados.
* **Hilt**: `@Singleton` para repositórios e `ImageLoader`; `@HiltViewModel` para as telas.

\---

## 7\. Modelo de dados

### 7.1 Entidade de capa

```kotlin
@Entity(tableName = "day\_covers")
data class DayCoverEntity(
    @PrimaryKey val dateKey: String,   // "2026-11-12" (data local)
    val mediaId: Long,
    val contentUri: String,            // URI completo, com volume
    val takenAtMillis: Long,           // cópia do DATE\_TAKEN (ou DATE\_ADDED \* 1000)
    val updatedAtMillis: Long,
)
```

```kotlin
@Dao
interface DayCoverDao {
    @Query("SELECT \* FROM day\_covers")
    fun observeAll(): Flow<List<DayCoverEntity>>

    @Query("SELECT \* FROM day\_covers WHERE dateKey = :dateKey")
    suspend fun findByDate(dateKey: String): DayCoverEntity?

    @Upsert
    suspend fun upsert(cover: DayCoverEntity)

    @Query("DELETE FROM day\_covers WHERE dateKey = :dateKey")
    suspend fun delete(dateKey: String)
}
```

**Decisões:**

* `dateKey` é a chave: legível, estável e imune a fuso horário *depois* de escolhida a data local.
* Guardar `contentUri` **completo** (e não apenas o `\_ID`) evita colisão de IDs entre volumes (interno e cartão SD). O `mediaId` fica como conveniência para validação e novas consultas.
* Não guardar bitmap nem cópia da foto: o app não duplica a galeria. Uma capa "sobrevive" à exclusão da foto apenas enquanto o cache do Coil existir; fora isso, mostramos placeholder.
* `takenAtMillis` serve para revalidar e para o visualizador; não é a chave.

### 7.2 Fotos: sempre do MediaStore

As fotos **não entram no banco**. O banco guarda apenas a escolha do usuário. As consultas são feitas por intervalo de datas e agrupadas por dia local.

```kotlin
data class DayPhotoSummary(
    val date: LocalDate,
    val count: Int,
    val latestMediaId: Long,
)
```

### 7.3 Consulta por mês

```kotlin
val zone = ZoneId.systemDefault()
val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
val end = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()

val projection = arrayOf(
    MediaStore.Images.Media.\_ID,
    MediaStore.Images.Media.DATE\_TAKEN,   // milissegundos
    MediaStore.Images.Media.DATE\_ADDED,   // segundos
)
val selection = """
    (DATE\_TAKEN >= ? AND DATE\_TAKEN < ?)
    OR ((DATE\_TAKEN IS NULL OR DATE\_TAKEN = 0) AND DATE\_ADDED >= ? AND DATE\_ADDED < ?)
""".trimIndent()
```

* `DATE\_TAKEN` vem em **milissegundos**; `DATE\_ADDED` vem em **segundos** — converter antes de comparar. Nos argumentos da consulta, use `start`/`end` (ms) para `DATE\_TAKEN` e `start / 1000`/`end / 1000` para `DATE\_ADDED`; misturar as unidades é o erro clássico aqui.
* Agrupar em Kotlin com `Instant.ofEpochMilli(...).atZone(zone).toLocalDate()` para respeitar o fuso local e evitar `strftime` com `localtime` (DST e fusos com offset de 30/45 min são traiçoeiros).
* Fotos sem `DATE\_TAKEN` (comum em downloads e exports) caem no `DATE\_ADDED`; o app aceita que a data seja a de inclusão no aparelho.
* Para API 29+, consultar `MediaStore.Images.Media.getContentUri(MediaStore.VOLUME\_EXTERNAL)` para incluir todos os volumes externos.

### 7.4 Consulta por dia

A tela do dia usa o mesmo intervalo (`\[00:00, 00:00 do dia seguinte)` local) e retorna as colunas necessárias para a grade (`\_ID`, `DATE\_TAKEN`, `DATE\_ADDED`, `WIDTH`, `HEIGHT`). Ordenar por data decrescente. O `contentUri` é montado com `ContentUris.withAppendedId(collectionUri, id)`.

### 7.5 Validação e foto removida

Na inicialização (e ao voltar do background), o repositório valida em lote os `mediaId` das capas com uma consulta `\_ID IN (...)`. O resultado é um conjunto de capas válidas ou inválidas em memória. Quando o URI armazenado tiver volume (cartão SD), validar também o volume/URI, para não confundir um ID repetido em outro volume.

* Capa inválida **não é apagada automaticamente** (a foto pode voltar após remontar um cartão SD).
* A célula mostra um placeholder; o dia mostra "Capa indisponível" e um botão para escolher outra.
* Se o usuário escolher outra, a linha é substituída.

### 7.6 Backup e restauração

* `android:allowBackup="true"` com regras que incluam o banco Room (é minúsculo).
* Na restauração, os IDs podem não corresponder às fotos do novo aparelho; a validação da seção 7.5 resolve sem crash, mas algumas capas podem ser perdidas.
* Exportar/importar um `.json` com `dateKey`, `mediaId`, `uri` e um hash leve é o caminho para uma restauração confiável — fica para o pós-MVP.

\---

## 8\. Permissões e privacidade

### 8.1 Matriz

|Versão do Android|Permissões declaradas|Comportamento|
|-|-|-|
|8.0–12 (API 26–32)|`READ\_EXTERNAL\_STORAGE` (`maxSdkVersion="32"`)|Acesso total à galeria|
|13 (API 33)|`READ\_MEDIA\_IMAGES`|Acesso total; sem acesso parcial|
|14+ (API 34+)|`READ\_MEDIA\_IMAGES` + `READ\_MEDIA\_VISUAL\_USER\_SELECTED`|O sistema oferece "Permitir tudo" ou "Selecionar fotos"|

```xml
<uses-permission android:name="android.permission.READ\_MEDIA\_IMAGES" />
<uses-permission android:name="android.permission.READ\_MEDIA\_VISUAL\_USER\_SELECTED" />
<uses-permission
    android:name="android.permission.READ\_EXTERNAL\_STORAGE"
    android:maxSdkVersion="32" />
```

### 8.2 Fluxo de onboarding

1. Tela de boas-vindas com três linhas: "Suas fotos, organizadas por dia", "Escolha uma capa para cada lembrança", "Tudo fica no seu aparelho".
2. Botão "Permitir acesso às fotos" → solicitação do sistema.
3. **Concedido:** calendário carrega.
4. **Negado:** cartão "Precisamos da sua galeria para montar o mural" + "Tentar de novo" + "Abrir configurações".
5. **Parcial (API 34+):** banner "Você compartilhou algumas fotos. O mural mostra só elas." + "Gerenciar".

```kotlin
fun hasPartialAccess(context: Context): Boolean =
    Build.VERSION.SDK\_INT >= 34 \&\&
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ\_MEDIA\_VISUAL\_USER\_SELECTED
        ) == PackageManager.PERMISSION\_GRANTED \&\&
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ\_MEDIA\_IMAGES
        ) != PackageManager.PERMISSION\_GRANTED
```

### 8.3 Detalhes que evitam bugs

* **Não** declarar `MANAGE\_EXTERNAL\_STORAGE`; a Play restringe e não é necessário.
* `READ\_EXTERNAL\_STORAGE` com `maxSdkVersion="32"` evita que ele apareça em aparelhos novos.
* No Android 14+, declarar `READ\_MEDIA\_VISUAL\_USER\_SELECTED` é o que permite o sistema oferecer a opção de seleção parcial; sem ela, o usuário pode ser forçado a "tudo ou nada".
* Re-solicitar permissão nem sempre reabre o seletor: ofereça também um link para as configurações do app.
* Testar em aparelhos Xiaomi, Samsung e Pixel: alguns OEMs mudam o texto e o comportamento do diálogo.
* O app **não declara `INTERNET`**. Isso é uma decisão de produto: nenhuma foto, metadado ou telemetria sai do aparelho.

### 8.4 Play Store

* `targetSdk` conforme a exigência vigente.
* Sem `INTERNET` e sem SDKs de analytics: a seção "Segurança dos dados" fica simples ("nenhum dado coletado").
* Política de privacidade curta, mesmo sem coleta: explica que o app lê a galeria localmente e não envia nada.
* Permissões de fotos são runtime permissions comuns; não exigem o formulário de acesso amplo a arquivos.

\---

## 9\. Calendário: especificação de UI

### 9.1 Matemática do mês

```kotlin
val month: YearMonth = YearMonth.now()
val firstDay: LocalDate = month.atDay(1)
val weekStart: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
val lead = (firstDay.dayOfWeek.value - weekStart.value + 7) % 7
val cells = List(lead) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
val padded = cells + List(42 - cells.size) { null } // 6 linhas × 7 colunas
val rows = 6 // sempre 6 para manter a altura estável entre meses
```

* Sempre renderizar **6 linhas** (células fantasma invisíveis no fim), para que o pager não mude de altura ao trocar de mês.
* A semana começa no domingo em pt-BR; a preferência pode ser ajustada depois via DataStore.
* Meses com 4 semanas (fevereiro não bissexto começando no domingo) continuam com 6 linhas: o espaço vazio faz parte do ritmo.

### 9.2 Pager de meses

```kotlin
val minMonth = /\* mês da foto mais antiga, ou atual - 24 meses \*/ 
val pageCount = ChronoUnit.MONTHS.between(minMonth, currentMonth).toInt() + 1
val pagerState = rememberPagerState(initialPage = pageCount - 1) { pageCount }
```

* `minMonth` é calculado uma vez (consulta `MIN(DATE\_TAKEN)`), com fallback de 24 meses se a consulta falhar.
* Limitar `pageCount` a um teto (ex.: 600 páginas) evita estados absurdos se um arquivo tiver data de 1970.
* `beyondViewportPageCount = 1` pré-compõe o mês vizinho sem carregar tudo.
* Título e conteúdo derivam de `pagerState.currentPage`; usar `derivedStateOf` para evitar recomposições desnecessárias.

### 9.3 Célula

|Propriedade|Especificação|
|-|-|
|Tamanho|Largura = `(tela − 2×16 dp − 6×4 dp) / 7`; altura igual (1:1)|
|Raio|10 dp|
|Espaço|4 dp entre células|
|Foto|`ContentScale.Crop`, decodificada no tamanho da célula|
|Scrim|Gradiente vertical: transparente até 38%, preto 62% no rodapé|
|Número|10 sp, `SemiBold`, branco, canto inferior direito, padding 4 dp / 3 dp, sombra leve|
|Hoje|Anel de 2 dp em terracota, 2 dp de afastamento; ponto de 4 dp no topo esquerdo|
|Vazio|Papel (`#F0E7D8`), número em `#968468`|
|Futuro|Papel mais claro, número 40% opaco; toque permitido (mostra dia vazio)|
|Sem capa|Sem foto e sem scrim; o número é o único conteúdo|
|Toque|Área inteira; `Role.Button`; feedback de ripple sutil|

**Por que 10 sp?** O pedido é que o número seja visível, mas reduzido. 10 sp com peso semibold e sombra é legível em 46–50 dp de célula e não compete com a foto. O scrim garante contraste ≥ 4.5:1 mesmo em fotos claras.

### 9.4 Tema e contraste

* Claro: papel quente, tinta marrom-escura, acento terracota.
* Escuro: fundo `#17130F`, células `#24201C`, número branco; o scrim fica um pouco mais forte (70%).
* O número nunca usa a cor do acento; o acento é reservado para "hoje" e ações.
* Fotos escuras ganham um scrim mais leve; fotos claras, mais forte. Um único gradiente fixo já resolve a maioria; medir com amostras reais.

### 9.5 Movimento

* Troca de mês: animação padrão do pager (250–300 ms).
* Definir capa: crossfade de 200 ms na célula; snackbar sobe.
* Toque: escala 0.97 por 90 ms.
* Transição calendário → dia: slide/fade de 220 ms. Shared element (foto da célula → cabeçalho) apenas se a versão do Compose estiver estável; caso contrário, adiar.

### 9.6 Acessibilidade

* `contentDescription` por célula: "12 de novembro, 6 fotos, capa definida" / "…, sem fotos".
* Ordem de foco do TalkBack: cabeçalho → grade → cartão "Hoje".
* Contraste mínimo 4.5:1 para textos; o número sobre foto depende do scrim — validar com fotos claras e escuras.
* Alvos de toque: a célula tem \~44–46 dp em telas de 360–390 dp, abaixo dos 48 dp recomendados. Com 7 colunas, 48 dp só é viável em telas maiores ou reduzindo padding/gaps. Alternativas: padding lateral de 12 dp e gaps de 4 dp (célula \~45 dp em 360 dp e \~49 dp em 390 dp) ou aceitar a densidade de calendário, comum nesse tipo de app. Decidir no M4 com teste de acessibilidade.
* Fonte em 200%: o número cresce para 20 sp dentro de uma célula de \~45 dp; ainda cabe, mas validar. Não truncar a data.

### 9.7 Performance

* **Uma consulta por mês**, não uma por célula.
* Projeção mínima (3 colunas) para contagens; projeção completa só no dia.
* Cache em memória LRU (`month → Map<LocalDate, DayPhotoSummary>`), invalidado por `ContentObserver` no MediaStore ou ao voltar do background.
* `AsyncImage` com `size` explícito, `crossfade(150)`, `memoryCacheKey` estável por `mediaId`.
* Pré-carregar apenas o mês vizinho quando o pager assentar; não pré-carregar o ano inteiro.
* Metas: primeiro frame do calendário < 800 ms após a permissão; rolagem a 60 fps (120 em telas compatíveis); memória < 150 MB com 10 mil fotos.
* Baseline Profile + Macrobenchmark no M5.

\---

## 10\. Fluxo de capa

### 10.1 Caminho feliz

1. Usuário toca no dia 12.
2. `DayViewModel` carrega as fotos do dia e a capa atual.
3. Toca na foto desejada → estado `selectedMediaId`.
4. Toca em "Definir" → `CoverRepository.setCover(dateKey, photo)`.
5. `@Upsert` grava; o `Flow` do calendário emite; a célula atualiza.
6. Volta ao calendário com snackbar "Capa de 12 de novembro definida" + "Desfazer".

### 10.2 Atomicidade e desfazer

* A gravação é uma única linha (`dateKey` é PK): não há estado intermediário.
* O "Desfazer" guarda a entidade anterior em memória (ou `null`) e faz `upsert`/`delete` conforme o caso.
* Se o app for morto entre a gravação e o undo, a capa nova permanece — comportamento aceitável.
* Não usar `SharedPreferences` para isso: o Room já é a fonte da verdade.

### 10.3 Casos de borda

|Situação|Comportamento|
|-|-|
|Foto da capa apagada|Placeholder na célula; dia mostra "Capa indisponível"; escolher outra substitui|
|Permissão revogada|Calendário mantém as capas em cache/placeholder; onboarding reaparece|
|Usuário troca de fuso|Agrupamento por data local pode mudar; capas ficam na data em que foram salvas (dateKey)|
|Foto em cartão SD removido|URI inválida → placeholder; validação não apaga a linha|
|Duas fotos no mesmo dia|Uma capa por dia; a escolha é explícita|
|Reinstalação|Backup pode restaurar o banco; IDs inválidos são descartados na validação|
|Fonte 200%|Número cresce; testar que não vaza da célula|
|Rotação|Estado preservado via ViewModel; pager mantém a página|

### 10.4 Por que não guardar no MediaStore

O MediaStore não permite que o app escreva metadados customizados nas fotos que não são dele (sem `WRITE\_EXTERNAL\_STORAGE` e sem ser o dono do arquivo). Escrever essas informações exigiria `MediaStore.createWriteRequest` e permissões extras, com UX pior e risco de conflito com outros apps. O Room é local, transacional e não toca nas fotos.

\---

## 11\. Cronograma

Premissa: 1 desenvolvedor, tempo integral, com Android Studio e um aparelho físico para testes. Se for meio período, dobrar o calendário.

|Marco|Entrega|Critério de conclusão|Estimativa|
|-|-|-|-|
|**M0 — Fundação**|Projeto Gradle, Compose, Hilt, Room, tema, navegação vazia, CI|`assembleDebug` verde; CI roda lint + testes; tema claro/escuro|1 dia|
|**M1 — Calendário**|Grade do mês, pager, setas, hoje, células vazias, dados falsos|Navegação entre meses sem jank; matemática de datas testada; layout estável|2 dias|
|**M2 — Fotos reais**|MediaStore, permissões, contagens, miniaturas, estados de acesso|Funciona em API 29/33/34/35; acesso parcial tratado; sem fotos não quebra|3 dias|
|**M3 — Capas**|Tela do dia, seleção, definir/remover, Room, snackbar/undo|Capa persiste e atualiza o calendário; foto apagada não derruba o app|3 dias|
|**M4 — Polimento**|Cartão "Hoje", animações, acessibilidade, modo escuro, performance|Fonte 200%, TalkBack, 60 fps; sem regressões visuais|2 dias|
|**M5 — Release**|Testes de UI/screenshot, baseline profile, ícone, build assinado, ficha da Play|APK/AAB assinado; checklist de publicação completo|2 dias|

**Total:** 13 dias de trabalho + 20% de folga ≈ **16 dias úteis** (3 a 4 semanas de calendário). A maior incerteza está no M2 (permissões e datas do MediaStore), não no visual.

### Sequência sugerida

* **Semana 1:** M0 + M1 com 20 capas falsas (drawables) e validação do "feeling" do mural em um aparelho.
* **Semana 2:** M2 + início do M3 com a galeria real.
* **Semana 3:** M3 + M4; teste com a galeria pessoal do usuário (milhares de fotos).
* **Semana 4:** M5, ajustes e publicação interna.

O protótipo do M1 é a melhor forma de validar duas coisas antes de investir no resto: **a densidade das células** e **a legibilidade do número** em fotos reais.

\---

## 12\. Testes e qualidade

### 12.1 Unitários (JVM)

* Matemática de mês: dias no mês, ano bissexto, mês começando em cada dia da semana, offset inicial, 6 linhas.
* `dateKey` ↔ `LocalDate` (ida e volta), fuso em virada de mês.
* Agrupamento de fotos: `DATE\_TAKEN` nulo/0 → `DATE\_ADDED`; `DATE\_ADDED` em segundos.
* `setCover`/`removeCover`: upsert, substituição, undo, validação de foto inexistente.
* ViewModel: estados de carregamento, permissão, seleção e snackbar (Turbine).

### 12.2 Banco e MediaStore

* Room em memória: upsert, delete, Flow emite, migração 1→2.
* `MatrixCursor` simulando MediaStore: projeção, seleção, ordenação, fotos sem data.
* Validação em lote: capa válida, inválida, volume diferente.

### 12.3 UI

* Compose UI Test: calendário mostra "12", toque abre o dia, selecionar habilita o botão.
* Semântica: `contentDescription` correto, ordem de foco, botão com papel de botão.
* Roborazzi: screenshot do calendário claro/escuro com capas; screenshot do dia com/sem capa.
* Teste de fonte 200% e rotação (captura de layout).

### 12.4 Manual

|Cenário|Aparelho/versão|
|-|-|
|Sem permissão, negação, "não perguntar mais"|Pixel API 34|
|Acesso parcial (Selecionar fotos)|Pixel API 34/35|
|Permissão antiga (`READ\_EXTERNAL\_STORAGE`)|API 29/30|
|Galeria vazia|qualquer|
|Galeria com 10 mil+ fotos|aparelho real|
|Foto de capa apagada durante o uso|qualquer|
|Fonte 200%, modo escuro, rotação|qualquer|
|OEM com diálogo customizado|Xiaomi/Samsung|

### 12.5 CI e Definition of Done

* GitHub Actions: `lint`, `testDebugUnitTest`, `assembleDebug` em cada PR.
* Definition of Done de cada marco: testes verdes, lint sem erros, sem crash nos fluxos principais, screenshot atualizado (quando houver), APK de demo gerado.
* Sem `INTERNET` no manifesto: um teste de CI pode falhar se alguém adicionar.
* Revisão de acessibilidade com Accessibility Scanner antes do M5.

\---

## 13\. Riscos e mitigação

|Risco|Prob.|Impacto|Mitigação|
|-|-|-|-|
|Acesso parcial no Android 14+ limita o mural|Alta|Alto|Onboarding claro, banner "Gerenciar", estados que funcionam com poucas fotos; testar em API 34/35|
|`DATE\_TAKEN` incorreto ou ausente|Média|Alto|Fallback para `DATE\_ADDED`; testar com fotos reais (WhatsApp, downloads, câmera); permitir correção manual no futuro|
|Performance com bibliotecas grandes|Média|Médio|Consulta por mês, projeção mínima, cache LRU, Coil no tamanho da célula, Macrobenchmark|
|Foto da capa removida|Alta|Médio|Placeholder, validação em lote, reescolha fácil; não apagar automaticamente|
|Reinstalação perde capas|Média|Médio|Auto Backup + validação; export/import no pós-MVP|
|Jank no pager por grade aninhada|Baixa|Médio|Não usar `LazyVerticalGrid` dentro do pager; grade fixa em `Column`/`Row` com 6 linhas|
|Permissões com comportamento de OEM|Média|Médio|Testar Xiaomi/Samsung; link para configurações; não assumir que o diálogo é padrão|
|Escopo crescer (backup, widget, legendas)|Alta|Alto|Lista de "fora do MVP" explícita; cada extra entra no backlog com estimativa|
|Fuso horário e viagens|Baixa|Baixo|Documentar que a data é local; capas ficam na `dateKey` escolhida|
|Legibilidade do número em fotos claras|Baixa|Médio|Scrim fixo + sombra; validar com amostras; ajustar alpha|
|Mural vazio no primeiro uso|Alta|Médio|Estado vazio convidativo, cartão "Hoje" e convite para escolher a primeira capa|
|Usuário não entende o conceito de "capa do dia"|Média|Médio|Microcopy consistente ("capa do dia"), selo "CAPA" e snackbar; testar com 5 pessoas|

\---

## 14\. Decisões em aberto

|#|Pergunta|Recomendação|Impacto|
|-|-|-|-|
|1|Nativo Kotlin/Compose ou multiplataforma?|**Nativo**|Performance, MediaStore e permissões; Flutter só com plano de iOS|
|2|Nome do app?|"Galeria Mural" (provisório) ou "Mural"|Identidade e ícone; não bloqueia o código|
|3|Capa automática para dias sem escolha?|**Não** no MVP; opcional "sugerir" depois|Evita surpresa e mantém o controle do usuário|
|4|Backup/sincronização?|**Local** no MVP; exportar `.json` depois|Privacidade e escopo|
|5|Até onde navegar para trás?|Mês da foto mais antiga, com fallback de 24 meses|Evita pager infinito e consulta cara no startup|
|6|Início da semana?|Domingo (pt-BR), com preferência futura|Afeta a grade e os testes|
|7|Grade do dia: 2 ou 3 colunas?|2 colunas até 9 fotos; 3 acima|Equilíbrio entre seleção e volume|
|8|Visualizador em tela cheia no MVP?|**Não**; toque seleciona, toque longo abre (M4)|Reduz escopo sem perder o fluxo principal|
|9|Mostrar dias futuros?|Sim, esmaecidos, mas sem fotos|Consistência do mês; não confunde|
|10|Idioma?|pt-BR apenas no MVP, com strings externalizadas|Permite i18n depois sem retrabalho|
|11|Fonte das fotos: galeria do aparelho ou álbum interno?|**Galeria do aparelho** (MediaStore)|Se for álbum interno, muda a camada de dados, o onboarding e o backup|

\---

## 15\. Próximos passos

1. **Validar as decisões 1, 2 e 4** (stack, nome, backup). São as únicas que mudam o plano de forma estrutural.
2. **M0 (1 dia):** criar o projeto, tema, navegação e CI. Sem tela de produto ainda.
3. **M1 (2 dias):** calendário com dados falsos e 20 capas de exemplo; testar em aparelho real e ajustar densidade/legibilidade.
4. **M2 (3 dias):** trocar dados falsos por MediaStore + permissões; testar em API 29, 33 e 34.
5. **M3 (3 dias):** tela do dia + capas + Room + undo.
6. **M4/M5:** polimento, testes visuais, baseline profile e build assinado.
7. **Teste de usabilidade (30 min, 5 pessoas):** pedir para "escolher a foto que representa o dia 12" e observar se entendem o selo, a seleção e o retorno ao calendário. Ajustar microcopy antes do M4.

> O maior risco do projeto não é o calendário — é a camada de permissões e datas do Android. Por isso o M2 vem antes de qualquer polish visual.

\---

### Anexo — Estrutura de exemplo de `CalendarUiState`

```kotlin
@Immutable
data class CalendarUiState(
    val month: YearMonth,
    val cells: List<DayCellUi>,
    val access: PhotoAccess,
    val isLoading: Boolean,
    val today: LocalDate,
)

@Immutable
data class DayCellUi(
    val date: LocalDate,
    val coverUri: String?,
    val photoCount: Int,
    val isToday: Boolean,
    val isFuture: Boolean,
    val isCoverAvailable: Boolean = true,
)

sealed interface PhotoAccess {
    data object Granted : PhotoAccess
    data object Partial : PhotoAccess
    data object Denied : PhotoAccess
    data object Unknown : PhotoAccess
}
```

