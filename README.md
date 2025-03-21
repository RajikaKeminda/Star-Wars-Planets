# Star Wars Planets Explorer

A modern Android application that displays information about planets from the Star Wars universe, built with Jetpack Compose and following Android architecture best practices.

## Features

- **Planet List**: View a scrollable list of all planets from the Star Wars universe
- **Planet Details**: See detailed information about each planet
- **Offline Support**: Access previously loaded planet data when offline
- **Pagination**: Load more planets as you scroll through the list
- **Modern UI**: Clean and intuitive interface built with Jetpack Compose





https://github.com/user-attachments/assets/8e76a927-c6d1-4eb4-829f-8f1da07f9aa1



## Technologies & Architecture

This application is built using:

- **Kotlin**: 100% Kotlin codebase
- **Jetpack Compose**: For modern, declarative UI
- **MVVM Architecture**: Clean separation of concerns
- **Dependency Injection**: Hilt for dependency injection
- **Coroutines & Flow**: For asynchronous operations
- **Retrofit**: For API requests
- **Room**: For local database storage
- **Coil**: For image loading
- **Repository Pattern**: For single source of truth
- **Unit & UI Testing**: Comprehensive test coverage

## Project Structure

The application follows a clean architecture approach:

```
app/
├── java/com/example/starwarsplanets/
│   ├── data/           # Data layer
│   │   ├── api/        # API related classes
│   │   ├── models/     # Data models
│   │   ├── local/      # Local database
│   │   └── repository/ # Repositories
│   ├── di/             # Dependency injection
│   ├── ui/             # UI layer
│   │   ├── components/ # Reusable UI components
│   │   ├── planetlist/ # Planet list screen
│   │   ├── details/    # Planet details screen
│   │   ├── splash/     # Splash screen
│   │   └── theme/      # App theme
│   └── util/           # Utility classes
```

## Getting Started

### Prerequisites

- Android SDK 24 or higher

### Installation

1. Clone the repository:
```bash
git clone https://github.com/RajikaKeminda/Star-Wars-Planets.git
```

2. Open the project in Android Studio

3. Build and run the application

## API

This application uses the [SWAPI (Star Wars API)](https://swapi.dev/) to fetch planet data.

## Testing

The project includes unit tests:

- **Unit Tests**: Tests for the repository, ViewModel, and utility classes

## Acknowledgments

- [SWAPI](https://swapi.dev/) for providing the Star Wars data
- [Picsum Photos](https://picsum.photos/) for placeholder images
- The Android Jetpack Compose team for the amazing UI toolkit
