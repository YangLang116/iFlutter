# shellcheck disable=SC2038
find ../src/main/resources/icons -name '*.png' | xargs -n1 file -F ' : ' | grep RGBA | awk '{print $1}' | xargs -n1 ./pngquant/pngquant --force --skip-if-larger --ext '.png'
find ../doc/configs -name '*.png' | xargs -n1 file -F ' : ' | grep RGBA | awk '{print $1}' | xargs -n1 ./pngquant/pngquant --force --skip-if-larger --ext '.png'
