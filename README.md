# EDSDK for Processing

This library for [Processing](http://www.processing.org) gives you full access to your Canon SLR camera via the EDSDK on Windows. The features include: 

- Taking and downloading pictures
- Manually adjusting focus
- Setting apperature, exposure, ISO
- Accessing live view



## Applying for SDK access
Before you can use this library you need to obtain the EDSDK native library from Canon. You can do so via their developers program: 

- [Canon Europe](http://www.didp.canon-europa.com/)
- [Canon USA](http://www.usa.canon.com/cusa/consumer/standard_display/sdk_homepage)
- [Canon Asia](http://www.canon-asia.com/personal/web/developerresource)
- [Canon Oceania](https://www.canon.co.nz/en-NZ/Personal/Support-Help/Support-News/Canon-SDK)

Once you were granted access - this may take a few days - download the latest version of their library and follow the usage instructions. 


## Usage instructions 

1. Make sure you are using the 32bit version of Processing for Windows
1. Download the edsdk processing library and place it in your libraries folder
1. Copy the entire `EDSDK/lib` folder to `libraries/edsdk/library`. 
   You should end up having with the dll in `libraries/edsdk/library/EDSDK/Dlls/EDSDK.dll`
1. Fire up processing and browse the examples

## Modifying this library

If you want to modify this library or just compile it for yourself you also need to download the `EDSDK`. Place it inside the edsdk4j subproject so that you end up with the dll file in `edsdk-processing/edsdk4j/EDSDK/Dlls/EDSDK.dll`.

Then run `ant` from the command line and watch the magic. 


## License

It's complicated. 

- This library itself is released under the [WTFPL](http://www.wtfpl.net/txt/copying/).
- JNA is dual licensed under the [LGPL2.1](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html)/[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
- EDSDK itself is proprietary. If you release a modified version publicly make sure you 
  don't include the EDSDK, as this is (afaik) not permitted by their terms.  