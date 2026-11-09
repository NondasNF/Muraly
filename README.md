# Muraly 📅🖼️

**Muraly** é um aplicativo Android nativo que transforma a galeria do seu celular em um calendário afetivo de recordações. Cada dia do mês é representado por um bloco no mural e a foto escolhida por você vira a capa daquele dia, acompanhada por um diário pessoal de anotações.

---

## 🌟 Principais Funcionalidades

- **Mural de Fotos em Calendário**:
  - Exibição mensal em grade fixa de 7 colunas × 6 linhas.
  - Destaque em tom terracota para o dia de **Hoje** e cartão com contagem diária de fotos.
  - Design inspirado em papel quente (`#FAF5ED` claro / `#17130F` escuro) com fontes serifadas elegantes.

- **Proporção Ajustável dos Blocos**:
  - Menu de configurações no cabeçalho (3 pontos `⋮`) para alternar a altura dos blocos:
    - `1:1 Quadrado`
    - `3:4 Retrato`
    - `2:3 Retrato alto`

- **Visualização Limpa de Fotos**:
  - Renderização das fotos de capa sem filtros escuros ou sombras sobrepostas.

- **Diário do Dia (Notas Pessoais)**:
  - Campo de texto no estilo papel para escrever lembretes, pensamentos e notas para qualquer data.
  - **Auto-expansível**: O campo cresce conforme você escreve sem barras de rolagem internas, acompanhando a rolagem da página.
  - Salvo automaticamente no banco de dados local.

- **Seleção Flexível de Capas**:
  - Alterne entre a aba *"Fotos do dia"* (fotos tiradas na data) e *"Todas as fotos"* (busca em toda a galeria).
  - Escolha qualquer foto da galeria para ser a capa de qualquer dia.

- **Ampliação em Tela Cheia**:
  - Clique no cartão de capa no topo da tela do dia para visualizar a foto em tela cheia com botão de fechar.

- **Desfazer Ações (Undo)**:
  - Snackbar com ação de desfazer por 5+ segundos ao definir ou remover capas.

- **Privacidade Radical & 100% Off-line**:
  - Nenhuma permissão de internet (`INTERNET` desativada).
  - Suporte completo às permissões parciais de fotos do Android 14+ (`READ_MEDIA_VISUAL_USER_SELECTED`).
  - Todas as fotos, capas e textos permanecem exclusivamente no seu aparelho.

---

## 🛠️ Tecnologia & Arquitetura

- **UI**: Jetpack Compose + Material 3 Design Tokens
- **Linguagem**: Kotlin 2.x
- **Arquitetura**: MVVM com Clean Architecture (Core / Data / Feature)
- **Injeção de Dependências**: Hilt
- **Banco de Dados**: Room Database + KSP (com suporte a migrações e preservação de dados)
- **Carregamento de Imagens**: Coil 3 (`coil-compose`)
- **Navegação**: Navigation Compose
- **Datas**: API nativa `java.time` (`minSdk = 26`)

---

## 📂 Estrutura do Projeto

```text
app/src/main/java/br/com/mural/
├── MuralApp.kt                 # Application class com Hilt e Coil 3 ImageLoader
├── MainActivity.kt             # Activity principal Edge-to-Edge
├── core/
│   ├── date/                   # DateUtils e cálculos de grade mensal
│   ├── permissions/            # Gerenciamento de permissões de galeria (API 26-35)
│   └── ui/theme/               # Tokens de cores (Papel/Terracota), Tipografia Serifada e Tema
├── data/
│   ├── covers/                 # Room Entity, DAO, Database e CoverRepository (Capas + Diário)
│   └── photos/                 # MediaStorePhotoSource e PhotoRepository (Consultas na galeria)
├── feature/
│   ├── calendar/               # Tela de Calendário, DayCell, TodayCard e ViewModel
│   └── day/                    # Tela do Dia, Diário, BottomSheet de Seleção e FullScreenViewer
└── navigation/
    ├── Routes.kt               # Definção de rotas
    └── MuralNavHost.kt         # NavHost do aplicativo
```

---

## 🚀 Como Compilar e Rodar

### Pré-requisitos
- **Android Studio** (Ladybug ou superior)
- **JDK 17**
- Dispositivo Android físico ou emulador com **minSdk 26** (Android 8.0+)

### Comandos do Gradle

```bash
# Compilar e instalar a versão Debug no dispositivo conectado:
./gradlew app:installDebug

# Gerar o APK de Release final otimizado para uso diário:
./gradlew app:assembleRelease

# Executar a suíte de testes unitários:
./gradlew app:testDebugUnitTest
```

O APK final de Release será gerado na pasta:
`app/build/outputs/apk/release/app-release.apk`

---

## 🛡️ Licença e Privacidade

O **Muraly** foi construído com foco total na privacidade do usuário. Nenhum dado, imagem ou informação pessoal sai do seu dispositivo.
