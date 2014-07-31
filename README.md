# EDSDK for Processing

This library for [Processing](http://www.processing.org) gives you full access to your Canon SLR camera via the EDSDK on Windows. The features include: 

- Taking and downloading pictures
- Setting apperature, exposure, ISO
- Accessing live view

------
**This is the sourcecode of edsdk-processing. If you are just  interested in the library follow these links:**

* Project website
[http://asdfg.me/up/edsdk-processing](http://asdfg.me/up/edsdk-processing)
* JavaDoc [http://asdfg.me/up/edsdk-processing/reference/index.html](http://asdfg.me/up/edsdk-processing/reference/index.html)
* Snippets library [http://asdfg.me/up/edsdk-processing/snippets](http://asdfg.me/up/edsdk-processing/snippets)

------


## Applying for SDK access
Before doing anything else you need to obtain the EDSDK native library from Canon. You can do so via their developers program: 

- [Canon Europe](http://www.didp.canon-europa.com/)
- [Canon USA](http://www.usa.canon.com/cusa/consumer/standard_display/sdk_homepage)
- [Canon Asia](http://www.canon-asia.com/personal/web/developerresource)
- [Canon Oceania](https://www.canon.co.nz/en-NZ/Personal/Support-Help/Support-News/Canon-SDK)

Once you were granted access - this may take a few days - download the latest version of their library and follow the usage instructions. 


## Development setup

1. Clone this repository
1. Pull in the edsdk submodule (run `git submodule foreach git pull`)
1. Download the EDSDK from Canon
1. Copy the entire `Windows\EDSDK` into the project. 
   You should end up having with the dll in `edsdk-processing/EDSDK/Dlls/EDSDK.dll`
1. Import the project in eclipse
1. Run the file `src/probe/HansiTest.java`
1. To export the plugin to processing go to the `resources` folder and run `ant`. 
1. This project is not much more than a wrapper around edsdk4j. You can find some documentation [here](https://github.com/kritzikratzi/edsdk4j). 

## License

It's complicated. 

- This library itself and edsdk4j are released under the [WTFPL](http://www.wtfpl.net/txt/copying/).
- JNA is dual licensed under the [LGPL2.1](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html)/[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
- EDSDK itself is proprietary. If you release a modified version publicly make sure you 
  don't include the EDSDK, as this is (afaik) not permitted by their terms.  